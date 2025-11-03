// TicTacToeVIPApp.swift
import SwiftUI

@main
struct TicTacToeVIPApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

// ContentView.swift
import SwiftUI

struct ContentView: View {
    var body: some View {
        GameView()
    }
}

// GameView.swift
import SwiftUI

struct GameView: View {
    @StateObject private var gameVM = GameViewModel()
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                gradient: Gradient(colors: [Color(red: 26/255, green: 42/255, blue: 108/255),
                                         Color(red: 178/255, green: 31/255, blue: 31/255),
                                         Color(red: 253/255, green: 187/255, blue: 45/255)]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 20) {
                    // Header
                    HeaderView(gameVM: gameVM)
                    
                    // Timer Section
                    TimerSectionView(gameVM: gameVM)
                    
                    // VIP Section
                    VIPSectionView(gameVM: gameVM)
                    
                    // Game Status
                    Text(gameVM.status)
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .frame(height: 36)
                    
                    // Game Board
                    GameBoardView(gameVM: gameVM)
                    
                    // Controls
                    ControlsView(gameVM: gameVM)
                    
                    // Locked Message
                    if gameVM.showLockedMessage {
                        LockedMessageView()
                    }
                    
                    Spacer()
                }
                .padding()
            }
        }
    }
}

// View Models
class GameViewModel: ObservableObject {
    @Published var currentPlayer = "X"
    @Published var gameBoard = Array(repeating: "", count: 9)
    @Published var gameActive = true
    @Published var isVIP = false
    @Published var timeLeft = 300
    @Published var status = "Player X's turn"
    @Published var showLockedMessage = false
    @Published var showAdminPanel = false
    @Published var vipStatus = ""
    
    private var timer: Timer?
    private let winningConditions = [
        [0, 1, 2], [3, 4, 5], [6, 7, 8],
        [0, 3, 6], [1, 4, 7], [2, 5, 8],
        [0, 4, 8], [2, 4, 6]
    ]
    
    init() {
        startTimer()
    }
    
    func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            if self.timeLeft > 0 {
                self.timeLeft -= 1
                if self.timeLeft <= 0 && !self.isVIP {
                    self.gameActive = false
                    self.showLockedMessage = true
                    self.status = "Game locked. Enter VIP code to continue."
                }
            }
        }
    }
    
    func makeMove(at index: Int) {
        guard gameActive && gameBoard[index].isEmpty else { return }
        if !isVIP && timeLeft <= 0 { return }
        
        gameBoard[index] = currentPlayer
        
        if checkWinner() {
            status = "Player \(currentPlayer) wins!"
            gameActive = false
            return
        }
        
        if isBoardFull() {
            status = "It's a tie!"
            gameActive = false
            return
        }
        
        currentPlayer = currentPlayer == "X" ? "O" : "X"
        status = "Player \(currentPlayer)'s turn"
    }
    
    private func checkWinner() -> Bool {
        for condition in winningConditions {
            let a = condition[0], b = condition[1], c = condition[2]
            if !gameBoard[a].isEmpty && gameBoard[a] == gameBoard[b] && gameBoard[a] == gameBoard[c] {
                return true
            }
        }
        return false
    }
    
    private func isBoardFull() -> Bool {
        return gameBoard.allSatisfy { !$0.isEmpty }
    }
    
    func newGame() {
        guard !showLockedMessage else { return }
        resetGame()
    }
    
    func resetGame() {
        guard !showLockedMessage else { return }
        gameBoard = Array(repeating: "", count: 9)
        gameActive = true
        currentPlayer = "X"
        status = "Player X's turn"
        
        if !isVIP {
            timeLeft = 300
        }
    }
    
    func activateVIP(code: String) {
        // VIP activation logic would go here
        isVIP = true
        timeLeft = 3600 // 60 minutes
        vipStatus = "VIP Status: ACTIVE (60 minutes)"
        showLockedMessage = false
        gameActive = true
        status = "VIP Activated! Player \(currentPlayer)'s turn"
    }
}

// Subviews
struct HeaderView: View {
    @ObservedObject var gameVM: GameViewModel
    
    var body: some View {
        VStack {
            Text("Tic Tac Toe")
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundColor(.white)
            
            Text("VIP Edition - Play without limits!")
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.9))
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(
            LinearGradient(
                gradient: Gradient(colors: [Color(red: 44/255, green: 62/255, blue: 80/255),
                                         Color(red: 74/255, green: 100/255, blue: 145/255)]),
                startPoint: .leading,
                endPoint: .trailing
            )
        )
        .cornerRadius(12)
    }
}

struct TimerSectionView: View {
    @ObservedObject var gameVM: GameViewModel
    
    var body: some View {
        VStack {
            Text("Game Timer")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(.white)
            
            Text(formatTime(gameVM.timeLeft))
                .font(.system(size: 40, weight: .bold, design: .monospaced))
                .foregroundColor(gameVM.timeLeft <= 60 ? .red : Color(red: 253/255, green: 187/255, blue: 45/255))
            
            Text("Time remaining in your session")
                .font(.caption)
                .foregroundColor(.white.opacity(0.8))
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(red: 44/255, green: 62/255, blue: 80/255))
        .cornerRadius(12)
    }
    
    private func formatTime(_ seconds: Int) -> String {
        let minutes = seconds / 60
        let seconds = seconds % 60
        return String(format: "%02d:%02d", minutes, seconds)
    }
}

struct VIPSectionView: View {
    @ObservedObject var gameVM: GameViewModel
    @State private var vipCode = ""
    
    var body: some View {
        VStack(spacing: 15) {
            Text("VIP Access")
                .font(.title3)
                .fontWeight(.semibold)
                .foregroundColor(Color(red: 44/255, green: 62/255, blue: 80/255))
            
            Text("Enter your VIP code to unlock unlimited play")
                .font(.body)
                .foregroundColor(.black)
                .multilineTextAlignment(.center)
            
            HStack {
                TextField("Enter VIP code", text: $vipCode)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                
                Button("Activate VIP") {
                    gameVM.activateVIP(code: vipCode)
                    vipCode = ""
                }
                .buttonStyle(VIPButtonStyle())
            }
            
            if !gameVM.vipStatus.isEmpty {
                Text(gameVM.vipStatus)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(Color(red: 44/255, green: 62/255, blue: 80/255))
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                    .background(Color(red: 253/255, green: 187/255, blue: 45/255))
                    .cornerRadius(20)
            }
        }
        .padding()
        .background(Color.white)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(red: 74/255, green: 100/255, blue: 145/255), style: StrokeStyle(lineWidth: 2, dash: [5]))
        )
        .cornerRadius(12)
    }
}

struct GameBoardView: View {
    @ObservedObject var gameVM: GameViewModel
    
    var body: some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 3), spacing: 10) {
            ForEach(0..<9, id: \.self) { index in
                CellView(value: gameVM.gameBoard[index]) {
                    gameVM.makeMove(at: index)
                }
            }
        }
        .aspectRatio(1, contentMode: .fit)
        .padding()
        .background(Color.white.opacity(0.1))
        .cornerRadius(12)
    }
}

struct CellView: View {
    let value: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(value)
                .font(.system(size: 40, weight: .bold))
                .foregroundColor(value == "X" ? .red : .blue)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .aspectRatio(1, contentMode: .fit)
                .background(Color.white.opacity(0.9))
                .cornerRadius(8)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct ControlsView: View {
    @ObservedObject var gameVM: GameViewModel
    
    var body: some View {
        HStack(spacing: 15) {
            Button("New Game") {
                gameVM.newGame()
            }
            .buttonStyle(GameButtonStyle())
            
            Button("Reset Game") {
                gameVM.resetGame()
            }
            .buttonStyle(GameButtonStyle())
        }
    }
}

struct LockedMessageView: View {
    var body: some View {
        VStack(spacing: 10) {
            Text("Game Locked!")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.white)
            
            Text("Your free session has ended. Please enter a VIP code to continue playing.")
                .font(.body)
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
            
            Text("For VIP purchases and any questions, email us at:")
                .font(.caption)
                .foregroundColor(.white.opacity(0.8))
            
            Text("dengluffy@gmail.com")
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.white)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color.red)
        .cornerRadius(12)
    }
}

// Custom Button Styles
struct GameButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(.white)
            .padding()
            .frame(maxWidth: .infinity)
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color(red: 44/255, green: 62/255, blue: 80/255),
                                             Color(red: 74/255, green: 100/255, blue: 145/255)]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(8)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}

struct VIPButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(.white)
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color(red: 44/255, green: 62/255, blue: 80/255),
                                             Color(red: 74/255, green: 100/255, blue: 145/255)]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(8)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}