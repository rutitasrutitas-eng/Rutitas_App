package mx.edu.uttt.rutitas.utils

import org.junit.Assert.*
import org.junit.Test

class AppConfigTest {
    @Test fun recognizesInstitutionalEmailsAndSubdomains() {
        assertTrue(AppConfig.isInstitutionalEmail("alumno@uttt.edu.mx"))
        assertTrue(AppConfig.isInstitutionalEmail("alumno@alumni.uaeh.edu.mx"))
        assertNotNull(AppConfig.institutionFor("docente@uttt.edu.mx"))
    }

    @Test fun rejectsImpersonationAndNonInstitutionalEmails() {
        assertFalse(AppConfig.isInstitutionalEmail("alumno@uttt.edu.mx.evil.example"))
        assertFalse(AppConfig.isInstitutionalEmail("alumno@gmail.com"))
        assertFalse(AppConfig.isInstitutionalEmail("sin-correo"))
    }
}
