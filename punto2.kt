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