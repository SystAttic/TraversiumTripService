package traversium.tripservice.security

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.auth.UserRecord
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import traversium.tripservice.service.FirebaseService


@TestConfiguration
class MockFirebaseConfig {

    @Bean
    @Primary
    fun firebaseAuth(): FirebaseAuth {
        val mockAuth = mock(FirebaseAuth::class.java)
        val mockToken = mock(FirebaseToken::class.java)
        val mockUserRecord = mock(UserRecord::class.java)

        `when`(mockToken.uid).thenReturn("firebase123")
        `when`(mockToken.email).thenReturn("test@example.com")
        `when`(mockToken.tenantId).thenReturn("default")

        `when`(mockUserRecord.uid).thenReturn("firebase123")
        `when`(mockUserRecord.email).thenReturn("test@example.com")
        `when`(mockUserRecord.photoUrl).thenReturn(null)

        `when`(mockAuth.verifyIdToken(any())).thenReturn(mockToken)
        `when`(mockAuth.getUser(any())).thenReturn(mockUserRecord)

        return mockAuth
    }

    @Bean
    @Primary
    fun firebaseService(firebaseAuth: FirebaseAuth): FirebaseService = FirebaseService(firebaseAuth)
}