package com.example.jobsterra.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jobsterra.R
import com.example.jobsterra.ui.theme.JobsTerraTheme
import kotlinx.coroutines.delay

@Composable
fun PantallaBienvenida(
    onGetStarted: () -> Unit = {}
) {
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

    val titleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 300
        ),
        label = "title_alpha"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 600
        ),
        label = "content_alpha"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    // Iniciar animaciones
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1976D2)) // Azul sólido sin degradado
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.weight(1f))

            // Logo/Icono real con acento naranja
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White // Fondo blanco para el logo
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "JobsTerra Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Título principal
            Text(
                text = "JobsTerra",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo con acento naranja
            Text(
                text = "Tu plataforma ideal para encontrar trabajo",
                fontSize = 18.sp,
                color = Color(0xFFFFCC80), // Naranja claro para el subtítulo
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = "Conecta con las mejores empresas y encuentra oportunidades que se adapten a tu perfil profesional",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .alpha(contentAlpha)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Características principales
            Column(
                modifier = Modifier.alpha(contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FeatureRow(
                    icon = "🔍",
                    text = "Busca entre miles de ofertas"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FeatureRow(
                    icon = "💼",
                    text = "Empresas verificadas"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FeatureRow(
                    icon = "🚀",
                    text = "Impulsa tu carrera profesional"
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Botón de comenzar con color naranja
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(buttonScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800) // Botón naranja
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    text = "Comenzar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto adicional
            Text(
                text = "¿Ya tienes cuenta? Inicia sesión",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureRow(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.95f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    JobsTerraTheme {
        PantallaBienvenida()
    }
}