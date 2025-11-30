package week11.st451951.nearbuy.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Login state, handles inputs, validation, and loading status
 */
private val firestore = FirebaseFirestore.getInstance()
data class LoginUIState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
)

/**
 * Registration state, handles inputs, validation, and loading status
 */
data class RegisterUIState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false
)

/**
 * Authentication events that are handled by UI when fired
 */
sealed class AuthEvent {
    data class ShowToast(val message: String) : AuthEvent()
    object NavigateToMain : AuthEvent()
    object NavigateToLogin : AuthEvent()
    data class RegistrationSuccess(val email: String) : AuthEvent()
}

/**
 * Authentication view model
 *
 * Backend responsible for auth functions such as logging in/out, registration, etc
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUIState())
    val loginState: StateFlow<LoginUIState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUIState())
    val registerState: StateFlow<RegisterUIState> = _registerState.asStateFlow()

    private val _authEvents = MutableSharedFlow<AuthEvent>()
    val authEvents = _authEvents.asSharedFlow()

    val isLoggedIn: Boolean
        get() = repository.isLoggedIn

    // ###################
    // LOGIN FIELD UPDATES
    // ###################
    fun updateLoginEmail(email: String) {
        _loginState.value = _loginState.value.copy(email = email, emailError = null)
    }

    fun updateLoginPassword(password: String) {
        _loginState.value = _loginState.value.copy(password = password, passwordError = null)
    }

    // ######################
    // REGISTER FIELD UPDATES
    // ######################
    fun updateRegisterName(name: String) {
        _registerState.value = _registerState.value.copy(name = name, nameError = null)
    }

    fun updateRegisterEmail(email: String) {
        _registerState.value = _registerState.value.copy(email = email, emailError = null)
    }

    fun updateRegisterPassword(password: String) {
        _registerState.value = _registerState.value.copy(password = password, passwordError = null)
    }

    fun updateRegisterConfirmPassword(confirmPassword: String) {
        _registerState.value = _registerState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null
        )
    }

    fun resetRegisterState() {
        _registerState.value = RegisterUIState()
    }

    fun resetLoginState() {
        _loginState.value = LoginUIState()
    }

    // ##########
    // VALIDATORS
    // ##########
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    // #######
    // SIGN IN
    // #######
    fun signIn() {
        val state = _loginState.value
        var hasError = false

        // Validate email
        if (!isValidEmail(state.email)) {
            _loginState.value = _loginState.value.copy(emailError = "Please enter a valid email")
            hasError = true
        }

        // Validate password
        if (!isValidPassword(state.password)) {
            _loginState.value = _loginState.value.copy(
                passwordError = "Password must be at least 6 characters"
            )
            hasError = true
        }

        if (hasError) {
            viewModelScope.launch {
                _authEvents.emit(AuthEvent.ShowToast("One or more fields are invalid"))
            }
            return
        }

        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true)

            repository.signIn(state.email, state.password)
                .onSuccess {
                    _authEvents.emit(AuthEvent.ShowToast("You are now signed in"))
                    _authEvents.emit(AuthEvent.NavigateToMain)

                    // Reset the login form so inputs aren't populated after logging out
                    resetLoginState()
                }
                .onFailure { e ->
                    _authEvents.emit(AuthEvent.ShowToast(e.message ?: "Sign in failed"))
                }

            _loginState.value = _loginState.value.copy(isLoading = false)
        }
    }

    // ########
    // REGISTER
    // ########
    fun register() {
        val state = _registerState.value
        var hasError = false

        // Validate name
        if (state.name.isBlank()) {
            _registerState.value = _registerState.value.copy(nameError = "Name is required")
            hasError = true
        }

        // Validate email
        if (!isValidEmail(state.email)) {
            _registerState.value = _registerState.value.copy(emailError = "Please enter a valid email")
            hasError = true
        }

        // Validate password
        if (!isValidPassword(state.password)) {
            _registerState.value = _registerState.value.copy(
                passwordError = "Password must be at least 6 characters"
            )
            hasError = true
        }

        // Validate password confirmation
        if (state.password != state.confirmPassword) {
            _registerState.value = _registerState.value.copy(
                confirmPasswordError = "Passwords do not match"
            )
            hasError = true
        }

        if (hasError) {
            viewModelScope.launch {
                _authEvents.emit(AuthEvent.ShowToast("One or more fields are invalid"))
            }
            return
        }

        viewModelScope.launch {
            _registerState.value = _registerState.value.copy(isLoading = true)

            repository.register(state.name, state.email, state.password)
                .onSuccess {
                    _authEvents.emit(AuthEvent.ShowToast("Your account was created successfully"))
                    _authEvents.emit(AuthEvent.RegistrationSuccess(state.email))
                    resetRegisterState()
                }
                .onFailure { e ->
                    _authEvents.emit(AuthEvent.ShowToast(e.message ?: "Registration failed"))
                }

            _registerState.value = _registerState.value.copy(isLoading = false)
        }
    }

    // ##############
    // PASSWORD RESET
    // ##############
    fun forgotPassword() {
        val state = _loginState.value

        // Just validate that a (valid) email has been entered
        if (!isValidEmail(state.email)) {
            _loginState.value = _loginState.value.copy(emailError = "Please enter a valid email")
            viewModelScope.launch {
                _authEvents.emit(AuthEvent.ShowToast("Please enter a valid email"))
            }
            return
        }

        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true)

            repository.sendPasswordResetEmail(state.email)
                .onSuccess {
                    _authEvents.emit(AuthEvent.ShowToast("An email to reset your password has been sent to you"))
                }
                .onFailure { e ->
                    _authEvents.emit(AuthEvent.ShowToast(e.message ?: "Failed to send reset email"))
                }

            _loginState.value = _loginState.value.copy(isLoading = false)
        }
    }

    fun checkAuthAndNavigate() {
        viewModelScope.launch {
            if (isLoggedIn) {
                _authEvents.emit(AuthEvent.NavigateToMain)
            }
        }
    }

    // ########
    // SIGN OUT
    // ########
    fun signOut() {
        repository.signOut()
        viewModelScope.launch {
            _authEvents.emit(AuthEvent.ShowToast("You have been signed out"))
            _authEvents.emit(AuthEvent.NavigateToLogin)
        }
    }

    // #######################
    // GET CURRENT USER'S NAME
    // #######################
    fun getCurrentUserName(): String {
        return repository.currentUser?.displayName ?: "User"
    }
}