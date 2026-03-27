package com.example.jobsterra.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jobsterra.R
import com.example.jobsterra.data.models.AuthState
import com.example.jobsterra.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun PantallaRegistro(
    authViewModel: AuthViewModel,
    isLoading: Boolean,
    authState: AuthState,
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    // Función para obtener mensaje de error amigable
    fun mensajeError(originalError: String?): String? {
        return when {
            originalError == null -> null
            originalError.contains("invalid-email", ignoreCase = true) ||
                    originalError.contains("badly formatted", ignoreCase = true) ->
                "Por favor, ingresa un email válido"

            originalError.contains("email-already-in-use", ignoreCase = true) ->
                "Este email ya está registrado"

            originalError.contains("weak-password", ignoreCase = true) ->
                "La contraseña debe tener al menos 6 caracteres"

            originalError.contains("network", ignoreCase = true) ||
                    originalError.contains("timeout", ignoreCase = true) ->
                "Error de conexión. Verifica tu internet"

            originalError.contains("operation-not-allowed", ignoreCase = true) ->
                "El registro está temporalmente deshabilitado"

            originalError.contains("too-many-requests", ignoreCase = true) ->
                "Demasiados intentos. Intenta de nuevo más tarde"

            else -> "Ocurrió un error inesperado. Intenta de nuevo"
        }
    }

    val errorMessage = localErrorMessage ?: if (authState is AuthState.Error) {
        mensajeError(authState.message)
    } else null

    val focusManager = LocalFocusManager.current

    // LaunchedEffect para observar el estado de autenticación
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onRegisterSuccess()
        }
    }

    // Animaciones
    var isVisible by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 200
        ),
        label = "content_alpha"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    fun handleRegister() {
        // Limpiar errores previos
        localErrorMessage = null

        // Validaciones del lado del cliente con mensajes amigables
        when {
            nombre.isBlank() -> {
                localErrorMessage = "Por favor, ingresa tu nombre completo"
                return
            }
            nombre.length < 2 -> {
                localErrorMessage = "El nombre debe tener al menos 2 caracteres"
                return
            }
            email.isBlank() -> {
                localErrorMessage = "Por favor, ingresa tu email"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                localErrorMessage = "Por favor, ingresa un email válido"
                return
            }
            password.isBlank() -> {
                localErrorMessage = "Por favor, ingresa tu contraseña"
                return
            }
            password.length < 6 -> {
                localErrorMessage = "La contraseña debe tener al menos 6 caracteres"
                return
            }
            confirmPassword.isBlank() -> {
                localErrorMessage = "Por favor, confirma tu contraseña"
                return
            }
            password != confirmPassword -> {
                localErrorMessage = "Las contraseñas no coinciden"
                return
            }
        }

        authViewModel.clearError()
        authViewModel.signUp(email, password, nombre)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1976D2))
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            // Logo real
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoScale),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo), // Cambia "logo" por el nombre de tu archivo
                        contentDescription = "JobsTerra Logo",
                        modifier = Modifier
                            .size(70.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Crear Cuenta",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Únete a JobsTerra y encuentra tu trabajo ideal",
                fontSize = 16.sp,
                color = Color(0xFFFFCC80),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha)
            ) {
                // Campo nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        // Limpiar errores al escribir
                        localErrorMessage = null
                        if (authState is AuthState.Error) {
                            authViewModel.clearError()
                        }

                        // Validación en tiempo real para nombre
                        if (nombre.isNotBlank() && nombre.length < 2) {
                            localErrorMessage = "El nombre debe tener al menos 2 caracteres"
                        }
                    },
                    label = { Text("Nombre completo", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Nombre",
                            tint = Color(0xFFFF9800)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color(0xFFFF9800)
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        // Limpiar errores al escribir
                        localErrorMessage = null
                        if (authState is AuthState.Error) {
                            authViewModel.clearError()
                        }

                        // Validación en tiempo real para email
                        if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            localErrorMessage = "Formato de email inválido"
                        }
                    },
                    label = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color(0xFFFF9800)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color(0xFFFF9800)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        // Limpiar errores al escribir
                        localErrorMessage = null
                        if (authState is AuthState.Error) {
                            authViewModel.clearError()
                        }

                        // Validación en tiempo real para contraseña
                        if (password.isNotBlank() && password.length < 6) {
                            localErrorMessage = "La contraseña debe tener al menos 6 caracteres"
                        }
                    },
                    label = { Text("Contraseña", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Contraseña",
                            tint = Color(0xFFFF9800)
                        )
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Text(
                                text = if (passwordVisible) "Ocultar" else "Mostrar",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color(0xFFFF9800)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo confirmar contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        // Limpiar errores al escribir
                        localErrorMessage = null
                        if (authState is AuthState.Error) {
                            authViewModel.clearError()
                        }

                        // Validación en tiempo real para confirmar contraseña
                        if (confirmPassword.isNotBlank() && password.isNotBlank() && password != confirmPassword) {
                            localErrorMessage = "Las contraseñas no coinciden"
                        }
                    },
                    label = { Text("Confirmar contraseña", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirmar contraseña",
                            tint = Color(0xFFFF9800)
                        )
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                        ) {
                            Text(
                                text = if (confirmPasswordVisible) "Ocultar" else "Mostrar",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF9800),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color(0xFFFF9800)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            handleRegister()
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Mensaje de error mejorado
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = errorMessage,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón registro
                Button(
                    onClick = { handleRegister() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Crear Cuenta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "  o  ",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para ir a login
                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        Color.White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}