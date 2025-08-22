// ==================== Modelo ====================

abstract class Persona(
    val nombreCompleto: String,
    val identificacion: String,
    val genero: String,
    val email: String
)

class Direccion(
    val calle: String,
    val numero: String,
    val barrio: String,
    val ciudad: String,
    val codigoPostal: String
)

class Paciente(
    nombreCompleto: String,
    identificacion: String,
    genero: String,
    email: String,
    val telefono: String,
    val direccion: Direccion
) : Persona(nombreCompleto, identificacion, genero, email)

class Medico(
    nombreCompleto: String,
    identificacion: String,
    genero: String,
    email: String,
    val licenciaProfesional: String,
    val especialidad: String,
    val anioIngreso: Int,
    val salario: Double
) : Persona(nombreCompleto, identificacion, genero, email) {
    val pacientesAsignados: MutableList<Paciente> = mutableListOf()
}

// ==================== Lógica de negocio ====================

class Hospital(
    val nombre: String,
    val NIT: String,
    val direccion: Direccion
) {
    private val medicos = mutableListOf<Medico>()
    private val pacientes = mutableListOf<Paciente>()

    // Conjuntos para garantizar unicidad (IDs y licencias)
    private val licenciasUsadas = mutableSetOf<String>()
    private val idsUsados = mutableSetOf<String>()

    // ---------- CRUD Médicos (con restricciones) ----------
    fun agregarMedico(medico: Medico): Boolean {
        if (medico.licenciaProfesional in licenciasUsadas) {
            println("Error: la licencia ${medico.licenciaProfesional} ya está registrada.")
            return false
        }
        if (medico.identificacion in idsUsados) {
            println("Error: el ID ${medico.identificacion} ya está registrado.")
            return false
        }
        medicos.add(medico)
        licenciasUsadas.add(medico.licenciaProfesional)
        idsUsados.add(medico.identificacion)
        return true
    }

    // Restricción: el hospital siempre debe tener al menos 1 médico activo
    fun eliminarMedico(id: String): Boolean {
        val medico = medicos.find { it.identificacion == id } ?: return false
        if (medicos.size <= 1) {
            println("Error: no se puede eliminar, el hospital debe tener al menos un médico activo.")
            return false
        }
        medicos.remove(medico)
        licenciasUsadas.remove(medico.licenciaProfesional)
        idsUsados.remove(medico.identificacion)
        return true
    }

    fun obtenerMedico(id: String): Medico? = medicos.find { it.identificacion == id }

    // Actualizar: reemplaza el médico (permite cambiar licencia/ID si no rompen unicidad)
    fun actualizarMedico(idActual: String, medicoNuevo: Medico): Boolean {
        val actual = medicos.find { it.identificacion == idActual } ?: return false

        val licenciaCambia = medicoNuevo.licenciaProfesional != actual.licenciaProfesional
        val idCambia = medicoNuevo.identificacion != actual.identificacion

        if (licenciaCambia && medicoNuevo.licenciaProfesional in licenciasUsadas) {
            println("Error: la licencia ${medicoNuevo.licenciaProfesional} ya está registrada.")
            return false
        }
        if (idCambia && medicoNuevo.identificacion in idsUsados) {
            println("Error: el ID ${medicoNuevo.identificacion} ya está registrado.")
            return false
        }

        medicos.remove(actual)
        medicos.add(medicoNuevo)
        if (licenciaCambia) {
            licenciasUsadas.remove(actual.licenciaProfesional)
            licenciasUsadas.add(medicoNuevo.licenciaProfesional)
        }
        if (idCambia) {
            idsUsados.remove(actual.identificacion)
            idsUsados.add(medicoNuevo.identificacion)
        }
        return true
    }

    // ---------- CRUD Pacientes (con restricción de ID único) ----------
    fun agregarPaciente(paciente: Paciente): Boolean {
        if (paciente.identificacion in idsUsados) {
            println("Error: el ID ${paciente.identificacion} ya está registrado.")
            return false
        }
        pacientes.add(paciente)
        idsUsados.add(paciente.identificacion)
        return true
    }

    fun eliminarPaciente(id: String): Boolean {
        val p = pacientes.find { it.identificacion == id } ?: return false
        pacientes.remove(p)
        idsUsados.remove(p.identificacion)
        // También remover de listas de asignación de médicos si aplica
        medicos.forEach { it.pacientesAsignados.removeIf { px -> px.identificacion == id } }
        return true
    }

    fun obtenerPaciente(id: String): Paciente? = pacientes.find { it.identificacion == id }

    fun actualizarPaciente(idActual: String, pacienteNuevo: Paciente): Boolean {
        val actual = pacientes.find { it.identificacion == idActual } ?: return false
        val idCambia = pacienteNuevo.identificacion != actual.identificacion

        if (idCambia && pacienteNuevo.identificacion in idsUsados) {
            println("Error: el ID ${pacienteNuevo.identificacion} ya está registrado.")
            return false
        }

        pacientes.remove(actual)
        pacientes.add(pacienteNuevo)
        if (idCambia) {
            idsUsados.remove(actual.identificacion)
            idsUsados.add(pacienteNuevo.identificacion)
        }

        // Actualizar asignaciones en médicos (si estaba asignado)
        medicos.forEach { medico ->
            val idx = medico.pacientesAsignados.indexOfFirst { it.identificacion == idActual }
            if (idx >= 0) medico.pacientesAsignados[idx] = pacienteNuevo
        }
        return true
    }

    // ---------- Consultas requeridas ----------
    fun totalSalarios(): String =
        String.format("%,.0f", medicos.sumOf { it.salario })

    fun totalSalariosPorEspecialidad(): Map<String, String> =
        medicos.groupBy { it.especialidad }
            .mapValues { (_, lista) -> String.format("%,.0f", lista.sumOf { it.salario }) }

    fun porcentajePacientesPorGenero(): Map<String, Double> {
        val total = pacientes.size.toDouble()
        return if (total == 0.0) emptyMap()
        else pacientes.groupBy { it.genero }
            .mapValues { (_, lista) -> (lista.size / total) * 100.0 }
    }

    fun cantidadMedicosPorEspecialidad(): Map<String, Int> =
        medicos.groupingBy { it.especialidad }.eachCount()

    fun medicoMasAntiguo(): Medico? = medicos.minByOrNull { it.anioIngreso }
}

// ==================== Ejemplo de uso ====================

fun main() {
    val direccionHospital = Direccion(
        "Avenida Bolívar con Calle 21",
        "Torre Principal 25N-80",
        "Centro",
        "Armenia",
        "630001"
    )
    val hospital = Hospital("Hospital San Juan de Armenia", "890123456-7", direccionHospital)

    // -------- 10 Médicos (solo 4 especialidades) --------
    val medicos = listOf(
        Medico("Carlos Andrés Ramírez", "M1001", "Masculino", "carlos.ramirez@HospitalArmenia.com", "LIC001", "Medicina Interna", 2008, 9_100_000.0),
        Medico("María Fernanda López", "M1002", "Femenino",  "maria.lopez@HospitalArmenia.com",   "LIC002", "Pediatría",        2016, 8_200_000.0),
        Medico("Jorge Enrique Martínez", "M1003", "Masculino","jorge.martinez@HospitalArmenia.com", "LIC003", "Cardiología",      2012, 9_500_000.0),
        Medico("Paula Andrea Sánchez",   "M1004", "Femenino", "paula.sanchez@HospitalArmenia.com",  "LIC004", "Ortopedia",        2014, 8_700_000.0),
        Medico("Andrés Felipe Castaño",  "M1005", "Masculino","andres.castano@HospitalArmenia.com", "LIC005", "Medicina Interna", 2009, 9_000_000.0),
        Medico("Laura Natalia Giraldo",  "M1006", "Femenino", "laura.giraldo@HospitalArmenia.com",  "LIC006", "Pediatría",        2018, 8_000_000.0),
        Medico("Camilo Andrés Herrera",  "M1007", "Masculino","camilo.herrera@HospitalArmenia.com", "LIC007", "Cardiología",      2015, 9_400_000.0),
        Medico("Valentina Morales",      "M1008", "Femenino", "valentina.morales@HospitalArmenia.com","LIC008","Ortopedia",       2017, 8_600_000.0),
        Medico("Daniel Alejandro Torres","M1009", "Masculino","daniel.torres@HospitalArmenia.com",  "LIC009", "Medicina Interna", 2010, 9_050_000.0),
        Medico("Natalia Andrea Ruiz",    "M1010", "Femenino", "natalia.ruiz@HospitalArmenia.com",   "LIC010", "Pediatría",        2019, 7_800_000.0)
    )
    medicos.forEach { hospital.agregarMedico(it) }

    // -------- 8 Pacientes --------
    val pacientes = listOf(
        Paciente("Juan Sebastián Gómez",    "P2001", "Masculino", "juan.gomez@gmail.com",     "3114567890",
            Direccion("Calle 50 Sur con Carrera 12", "Casa 18 Conjunto Los Naranjos", "La Castellana", "Armenia", "630002")),
        Paciente("Laura Vanessa Rodríguez", "P2002", "Femenino",  "laura.rodriguez@gmail.com", "3109876543",
            Direccion("Carrera 20 con Calle 40", "Apto 502 Torre B", "Quintas de San Pedro", "Armenia", "630003")),
        Paciente("Andrés Felipe Torres",    "P2003", "Masculino", "andres.torres@gmail.com",  "3007654321",
            Direccion("Calle 10 con Carrera 15", "Bloque 2 Apto 103", "Parque Residencial", "Armenia", "630004")),
        Paciente("Valentina Morales",       "P2004", "Femenino",  "valentina.morales@gmail.com","3129871234",
            Direccion("Carrera 25 con Calle 19", "Oficina 301 Ed. Tempo", "Centro", "Armenia", "630005")),
        Paciente("Camilo Andrés Herrera",   "P2005", "Masculino", "camilo.herrera@gmail.com", "3136549870",
            Direccion("Calle 30 con Carrera 8", "Casa 12 Manzana C", "La Alhambra", "Armenia", "630006")),
        Paciente("Natalia Andrea Ruiz",     "P2006", "Femenino",  "natalia.ruiz@gmail.com",   "3201234567",
            Direccion("Carrera 8 con Calle 22", "Torre 7 Apto 401", "Palermo", "Armenia", "630007")),
        Paciente("Santiago Esteban López",  "P2007", "Masculino", "santiago.lopez@gmail.com", "3004567891",
            Direccion("Carrera 18 con Calle 33", "Casa 25 Conjunto Campestre", "Villa Liliana", "Armenia", "630008")),
        Paciente("Mariana Juliana Quintero","P2008", "Femenino",  "mariana.quintero@gmail.com","3157896543",
            Direccion("Calle 15 con Carrera 21", "Apto 201 Torre 3", "Bosques de la Villa", "Armenia", "630009"))
    )
    pacientes.forEach { hospital.agregarPaciente(it) }

    // -------- Asignación de pacientes a médicos (ejemplo) --------
    // Repartimos 8 pacientes entre 5 médicos distintos
    medicos[0].pacientesAsignados.addAll(listOf(pacientes[0], pacientes[1])) // Medicina Interna
    medicos[1].pacientesAsignados.addAll(listOf(pacientes[2], pacientes[3])) // Pediatría
    medicos[2].pacientesAsignados.add(pacientes[4])                          // Cardiología
    medicos[3].pacientesAsignados.add(pacientes[5])                          // Ortopedia
    medicos[4].pacientesAsignados.addAll(listOf(pacientes[6], pacientes[7])) // Medicina Interna

    // -------- Consultas requeridas --------
    
    println("-------- Consultas requeridas --------\n\n")
    println("Total salarios: ${hospital.totalSalarios()}")
    println("Salarios por especialidad: ${hospital.totalSalariosPorEspecialidad()}")
    println("Pacientes por género (%): ${hospital.porcentajePacientesPorGenero()}")
    println("Médicos por especialidad: ${hospital.cantidadMedicosPorEspecialidad()}")

    hospital.medicoMasAntiguo()?.let {
        println("Médico más antiguo: ${it.nombreCompleto} | Especialidad: ${it.especialidad} | Año ingreso: ${it.anioIngreso}\n")
    }


    // -------- Pruebas rápidas de restricciones --------
    println("-------- Pruebas de restricciones --------\n\n")
    // Intentar agregar licencia duplicada
    val licenciaDuplicada = Medico(
        "Prueba Licencia Duplicada", "M9999", "Masculino", "prueba@HospitalArmenia.com",
        "LIC001", "Cardiología", 2020, 7_500_000.0
    )
    println("Agregar médico con licencia duplicada: ${hospital.agregarMedico(licenciaDuplicada)}")

    // Intentar eliminar dejando 0 médicos (debería fallar si solo quedara 1)

    val ids = (1001..1010).map { "M$it" }
    ids.dropLast(2).forEach { hospital.eliminarMedico(it) }

    println("Eliminar penúltimo médico: ${hospital.eliminarMedico(ids[ids.size-2])}")
    println("Eliminar último médico: ${hospital.eliminarMedico(ids.last())}")

}