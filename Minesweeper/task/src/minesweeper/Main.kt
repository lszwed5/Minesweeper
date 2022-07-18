package minesweeper

import kotlin.random.Random


const val SIZE = 9
var GAME_OVER = false

class Cell(var isMine: Boolean = false) {
    var minesAround = 0
    var isMarked = false
    var isExplored = false
    var display = "."
}


class Board {
    private var field = MutableList(SIZE + 2) { MutableList(SIZE + 2) {Cell(false)} }

    fun drawBoard() {
        println(" │123456789|")
        println("—│—————————│")
        for (row in 1 until SIZE + 1) {
            print("$row|")
            for (column in 1 until SIZE + 1) {
                if (!field[column][row].isExplored) {
                    if (field[column][row].isMarked) field[column][row].display = "*"
                    else field[column][row].display = "."
                }
                else {
                    if (field[column][row].isMine) field[column][row].display = "X"
                    else {
                        if (field[column][row].minesAround == 0) field[column][row].display = "/"
                        else field[column][row].display = field[column][row].minesAround.toString()
                    }
                }
                print(field[column][row].display)
            }
            print('|')
            println()
        }
        println("—│—————————│")
    }

    fun generateMines(numberOfMines: Int): MutableList<MutableList<Int?>> {
        // Randomize the placement of mines
        var xCoordinate = Random.nextInt(1, SIZE + 1)
        var yCoordinate = Random.nextInt(1, SIZE + 1)
        val mineCoordinates: MutableList<MutableList<Int?>> = mutableListOf(mutableListOf(xCoordinate, yCoordinate))
        for (mine in 2..numberOfMines) {
            while (true) {
                xCoordinate = Random.nextInt(1, SIZE + 1)
                yCoordinate = Random.nextInt(1, SIZE + 1)
                if (mutableListOf(xCoordinate, yCoordinate) !in mineCoordinates) break
            }
            mineCoordinates.add(mutableListOf(xCoordinate, yCoordinate))
        }

        // Update the gameBoard
        for (row in 1 until SIZE + 1) {
            for (column in 1 until SIZE + 1) {
                if (mutableListOf(column, row) in mineCoordinates) field[column][row].isMine = true
            }
        }
        return mineCoordinates
    }

    fun countSurroundingMines() {
        for (row in 1 until SIZE + 1) {
            for (column in 1 until SIZE + 1) {
                for (internalRow in row - 1..row + 1) {
                    for (internalColumn in column - 1..column + 1) {
                        if (internalRow == row && internalColumn == column) continue
                        else if (field[internalColumn][internalRow].isMine) field[column][row].minesAround += 1
                    }
                }
            }
        }
    }

    private fun markTheCell(x: Int?, y: Int?, markedCells: MutableList<MutableList<Int?>> = mutableListOf(mutableListOf(null))):
            MutableList<MutableList<Int?>> {
    markedCells.remove(mutableListOf(null))
        if (x!! in 1..9 && y!! in 1..9) {
            when (field[x][y].display) {
                "." -> {
                    field[x][y].isMarked = true
                    drawBoard()
                    markedCells.add(mutableListOf(x, y))
                    return markedCells
                }
                "*" -> {
                    field[x][y].isMarked = false
                    drawBoard()
                    markedCells.remove(mutableListOf(x, y))
                    return markedCells
                }
                else -> println("There is a number here!")
            }
        } else  {
            println("Coordinates out of the game board")
        }
        return mutableListOf(mutableListOf(null))
    }

    private fun exploreTheCell(x: Int?, y: Int?, mineCoordinates: MutableList<MutableList<Int?>>,
                               markedCells: MutableList<MutableList<Int?>> = mutableListOf(mutableListOf(null)),
                               isRecurrent: Boolean = false) {
        markedCells.remove(mutableListOf(null))
        if (!field[x!!][y!!].isExplored && x in 1..9 && y in 1..9) {
            if (field[x][y].isMine && !isRecurrent) {
                for (mineCoordinate in mineCoordinates) {
                    field[mineCoordinate[0]!!][mineCoordinate[1]!!].isExplored = true
                }
                GAME_OVER = true
                return
            } else {
                if (field[x][y].isMarked) markTheCell(x, y, markedCells)
                if (field[x][y].minesAround != 0) field[x][y].isExplored = true
                else {
                    for (internalRow in y - 1..y + 1) {
                        for (internalColumn in x - 1..x + 1) {
                            if (internalRow == y && internalColumn == x) continue
                            else {
                                field[x][y].isExplored = true
                                exploreTheCell(internalColumn, internalRow, mineCoordinates, markedCells, true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun makeAMove(markedCellsParam: MutableList<MutableList<Int?>> = mutableListOf(mutableListOf(null)),
                  mineCoordinates: MutableList<MutableList<Int?>>): MutableList<MutableList<Int?>> {
        markedCellsParam.remove(mutableListOf(null))
        var markedCells = markedCellsParam
        while (true) {
            print("Set/unset mine marks or claim a cell as free: ")
            val helper = readln().split(" ").toMutableList()
            val playerCoordinates = mutableListOf(helper[0].toIntOrNull(), helper[1].toIntOrNull())

            if (helper[2] == "mine") {
                markedCells = markTheCell(playerCoordinates[0]!!, playerCoordinates[1]!!, markedCells)
                if (markedCells != mutableListOf(mutableListOf(null))) return markedCells
            }

            else if (helper[2] == "free") {
                exploreTheCell(playerCoordinates[0]!!, playerCoordinates[1]!!, mineCoordinates, markedCells)
                drawBoard()
                return markedCells
            }

            else println("Unknown command")
        }
    }

    fun countExploredCells(): Int {
        var numberOfExploredCells = 0
        for (row in 1 until SIZE + 1) {
            for (column in 1 until SIZE + 1) {
                if (field[column][row].isExplored) numberOfExploredCells += 1
            }
        }
        return numberOfExploredCells
    }
}


fun main() {
    println("How many mines do you want on the field?")
    val numberOfMines = readln().toInt()

    val gameBoard = Board()
    val mineCoordinates: MutableList<MutableList<Int?>> = gameBoard.generateMines(numberOfMines)
    gameBoard.countSurroundingMines()
    gameBoard.drawBoard()

    var markedCells = gameBoard.makeAMove(mineCoordinates = mineCoordinates)
    var running = !GAME_OVER
    var numberOfExploredCells = gameBoard.countExploredCells()
    if (SIZE*SIZE - numberOfExploredCells == mineCoordinates.size) running = false


    while (running) {
        var counter = 0
        for (mineCoordinate in mineCoordinates) {
            if (mineCoordinate in markedCells) counter += 1
        }
        if (mineCoordinates.size == markedCells.size &&
            markedCells.size == counter) running = false
        else {
            markedCells = gameBoard.makeAMove(markedCells, mineCoordinates)
            if (GAME_OVER) running = false
        }
        numberOfExploredCells = gameBoard.countExploredCells()
        if (SIZE*SIZE - numberOfExploredCells == mineCoordinates.size) break
    }

    if (GAME_OVER) println("You stepped on a mine and failed!")
    else println("Congratulations! You found all the mines!")
}
