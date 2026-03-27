package com.example.jobsterra.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jobsterra.ui.screens.PantallaBienvenida
import com.example.jobsterra.ui.screens.PantallaHome
import com.example.jobsterra.ui.screens.PantallaLogin
import com.example.jobsterra.ui.screens.PantallaRegistro
import com.example.jobsterra.ui.screens.PantallaOferta
import com.example.jobsterra.ui.viewmodels.AuthViewModel
import com.example.jobsterra.ui.screens.PantallaMisOfertas
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.jobsterra.ui.screens.PantallaComunidad
import com.example.jobsterra.ui.screens.PantallaConfiguracion
import com.example.jobsterra.ui.screens.PantallaMiPerfil

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object MisOfertas : Screen("mis_ofertas")
    object Oferta : Screen("oferta/{ofertaId}")
    object Chats : Screen("chats")
    object Comunidad : Screen("comunidad")
    object Perfil : Screen("perfil")
    object Configuracion : Screen("configuracion")

}

@Composable
fun JobsTerraNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            PantallaBienvenida(
                onGetStarted = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Login.route) {
            println(">> authState en Login: $authState")
            PantallaLogin(
                authViewModel = authViewModel,
                isLoading = isLoading,
                authState = authState,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            PantallaRegistro(
                authViewModel = authViewModel,
                isLoading = isLoading,
                authState = authState,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            // Verificar que el usuario esté autenticado antes de mostrar Home
            when (val currentAuthState = authState) {
                is com.example.jobsterra.data.models.AuthState.Authenticated -> {
                    PantallaHome(
                        usuario = currentAuthState.user,
                        authViewModel = authViewModel,
                        ofertasViewModel = viewModel(),
                        onNavigateToMisOfertas = {
                            navController.navigate(Screen.MisOfertas.route)
                        },
                        onNavigateToChats = {
                            navController.navigate(Screen.Chats.route)
                        },
                        onNavigateToComunidad = {
                            navController.navigate(Screen.Comunidad.route)
                        },
                        onNavigateToPerfil = {
                            navController.navigate(Screen.Perfil.route)
                        },
                        onNavigateToConfiguracion = {
                            navController.navigate(Screen.Configuracion.route)
                        },
                        onNavigateToOferta = { ofertaId ->
                            navController.navigate("oferta/$ofertaId")
                        }
                    )
                }
                else -> {
                    // Si no está autenticado, redirigir al login
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                    // Mostrar pantalla de carga mientras redirige
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }
        }
        composable(Screen.MisOfertas.route) {
            when (val currentAuthState = authState) {
                is com.example.jobsterra.data.models.AuthState.Authenticated -> {
                    PantallaMisOfertas(
                        usuario = currentAuthState.user,
                        authViewModel = authViewModel,
                        ofertasViewModel = viewModel(),
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route)
                        },
                        onNavigateToChats = {
                            navController.navigate(Screen.Chats.route)
                        },
                        onNavigateToComunidad = {
                            navController.navigate(Screen.Comunidad.route)
                        },
                        onNavigateToPerfil = {
                            navController.navigate(Screen.Perfil.route)
                        },
                        onNavigateToOferta = { ofertaId ->
                            navController.navigate("oferta/$ofertaId")
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route)
                    }
                }
            }
        }
        composable(Screen.Perfil.route) {
            when (val currentAuthState = authState) {
                is com.example.jobsterra.data.models.AuthState.Authenticated -> {
                    PantallaMiPerfil(
                        usuario = currentAuthState.user,
                        authViewModel = authViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route)
                    }
                }
            }

        }

        composable(
            route = "oferta/{ofertaId}",
            arguments = listOf(navArgument("ofertaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ofertaId = backStackEntry.arguments?.getInt("ofertaId") ?: 0

            when (val currentAuthState = authState) {
                is com.example.jobsterra.data.models.AuthState.Authenticated -> {
                    PantallaOferta(
                        ofertaId = ofertaId,
                        ofertasViewModel = viewModel(),
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route)
                    }
                }
            }
        }
        composable(Screen.Configuracion.route) {
            when (val currentAuthState = authState) {
                is com.example.jobsterra.data.models.AuthState.Authenticated -> {
                    PantallaConfiguracion(
                        usuario = currentAuthState.user,
                        authViewModel = authViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route)
                    }
                }
            }
        }
        composable(Screen.Chats.route) {
            // TODO: Crear PantallaChats
            Text("Pantalla Chats - En construcción")
        }

        composable(Screen.Comunidad.route) {
            when (val currentAuthState = authState) {
                is com.example.jobsterra.data.models.AuthState.Authenticated -> {
                    PantallaComunidad(
                        usuario = currentAuthState.user,
                        authViewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route)
                        },
                        onNavigateToMisOfertas = {
                            navController.navigate(Screen.MisOfertas.route)
                        },
                        onNavigateToChats = {
                            navController.navigate(Screen.Chats.route)
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route)
                    }
                }
            }
        }


    }
}