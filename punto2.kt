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