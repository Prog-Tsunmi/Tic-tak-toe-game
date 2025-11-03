// MainActivity.kt
package com.example.tictactoevip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeVIPTheme {
                GameScreen()
            }
        }
    }
}

@Composable
fun TicTacToeVIPTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme.light(),
        content = content
    )
}

@Composable
fun GameScreen() {
    var gameState by remember { mutableStateOf(GameState()) }
    
    LaunchedEffect(key1 = gameState.isVIP, key2 = gameState.timeLeft) {
        while (true) {
            delay(1000)
            if (gameState.timeLeft > 0) {
                gameState = gameState.copy(timeLeft = gameState.timeLeft - 1)
                if (gameState.timeLeft <= 0 && !gameState.isVIP) {
                    gameState = gameState.copy(
                        gameActive = false,
                        showLockedMessage = true,
                        status = "Game locked. Enter VIP code to continue."
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1a2a6c),
                        Color(0xFFb21f1f),
                        Color(0xFFfdbb2d)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            HeaderSection()

            Spacer(modifier = Modifier.height(20.dp))

            // Timer Section
            TimerSection(gameState)

            Spacer(modifier = Modifier.height(20.dp))

            // VIP Section
            VIPSection(gameState) { code ->
                gameState = gameState.activateVIP(code)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Game Status
            Text(
                text = gameState.status,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.height(36.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Game Board
            GameBoard(gameState) { index ->
                if (gameState.gameActive && gameState.gameBoard[index].isEmpty()) {
                    if (!gameState.isVIP && gameState.timeLeft <= 0) return@GameBoard
                    
                    val newBoard = gameState.gameBoard.toMutableList()
                    newBoard[index] = gameState.currentPlayer
                    
                    val newState = gameState.copy(
                        gameBoard = newBoard.toList()
                    ).checkGameResult()
                    
                    gameState = if (newState.gameActive) {
                        newState.copy(
                            currentPlayer = if (gameState.currentPlayer == "X") "O" else "X",
                            status = "Player ${if (gameState.currentPlayer == "X") "O" else "X"}'s turn"
                        )
                    } else {
                        newState
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Controls
            ControlsSection(gameState) { action ->
                gameState = when (action) {
                    GameAction.NEW_GAME -> gameState.newGame()
                    GameAction.RESET_GAME -> gameState.resetGame()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Locked Message
            if (gameState.showLockedMessage) {
                LockedMessageSection()
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2c3e50),
                            Color(0xFF4a6491)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tic Tac Toe",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "VIP Edition - Play without limits!",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun TimerSection(gameState: GameState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2c3e50)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game Timer",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Text(
                text = formatTime(gameState.timeLeft),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = if (gameState.timeLeft <= 60) Color(0xFFe74c3c) else Color(0xFFfdbb2d),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            
            Text(
                text = "Time remaining in your session",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun VIPSection(gameState: GameState, onActivateVIP: (String) -> Unit) {
    var vipCode by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            2.dp,
            Color(0xFF4a6491),
            strokePathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VIP Access",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2c3e50)
            )
            
            Text(
                text = "Enter your VIP code to unlock unlimited play",
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = vipCode,
                    onValueChange = { vipCode = it },
                    placeholder = { Text("Enter VIP code") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF4a6491),
                        unfocusedIndicatorColor = Color(0xFF4a6491)
                    )
                )
                
                Button(
                    onClick = {
                        onActivateVIP(vipCode)
                        vipCode = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2c3e50)
                    )
                ) {
                    Text("Activate VIP")
                }
            }
            
            if (gameState.vipStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = gameState.vipStatus,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2c3e50),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .background(
                            color = Color(0xFFfdbb2d),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun GameBoard(gameState: GameState, onCellClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(gameState.gameBoard) { index, value ->
                Cell(value = value) {
                    onCellClick(index)
                }
            }
        }
    }
}

@Composable
fun Cell(value: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = if (value == "X") Color(0xFFe74c3c) else Color(0xFF3498db)
            )
        }
    }
}

@Composable
fun ControlsSection(gameState: GameState, onAction: (GameAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Button(
            onClick = { onAction(GameAction.NEW_GAME) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2c3e50)
            )
        ) {
            Text("New Game", fontSize = 16.sp)
        }
        
        Button(
            onClick = { onAction(GameAction.RESET_GAME) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2c3e50)
            )
        ) {
            Text("Reset Game", fontSize = 16.sp)
        }
    }
}

@Composable
fun LockedMessageSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFe74c3c)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game Locked!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Your free session has ended. Please enter a VIP code to continue playing.",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "For VIP purchases and any questions, email us at:",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Text(
                text = "dengluffy@gmail.com",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// Data classes and utilities
data class GameState(
    val currentPlayer: String = "X",
    val gameBoard: List<String> = List(9) { "" },
    val gameActive: Boolean = true,
    val isVIP: Boolean = false,
    val timeLeft: Int = 300,
    val status: String = "Player X's turn",
    val showLockedMessage: Boolean = false,
    val vipStatus: String = ""
) {
    fun checkGameResult(): GameState {
        val winningConditions = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        
        for (condition in winningConditions) {
            val (a, b, c) = condition
            if (gameBoard[a].isNotEmpty() && gameBoard[a] == gameBoard[b] && gameBoard[a] == gameBoard[c]) {
                return copy(
                    gameActive = false,
                    status = "Player $currentPlayer wins!"
                )
            }
        }
        
        if (gameBoard.all { it.isNotEmpty() }) {
            return copy(
                gameActive = false,
                status = "It's a tie!"
            )
        }
        
        return this
    }
    
    fun newGame(): GameState {
        if (showLockedMessage) return this
        return copy(
            gameBoard = List(9) { "" },
            gameActive = true,
            currentPlayer = "X",
            status = "Player X's turn"
        )
    }
    
    fun resetGame(): GameState {
        if (showLockedMessage) return this
        return copy(
            gameBoard = List(9) { "" },
            gameActive = true,
            currentPlayer = "X",
            status = "Player X's turn",
            timeLeft = if (!isVIP) 300 else timeLeft
        )
    }
    
    fun activateVIP(code: String): GameState {
        // In a real app, you'd validate the code against a database
        if (code.isNotEmpty()) {
            return copy(
                isVIP = true,
                timeLeft = 3600,
                vipStatus = "VIP Status: ACTIVE (60 minutes)",
                showLockedMessage = false,
                gameActive = true,
                status = "VIP Activated! Player $currentPlayer's turn"
            )
        }
        return this
    }
}

enum class GameAction {
    NEW_GAME, RESET_GAME
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}