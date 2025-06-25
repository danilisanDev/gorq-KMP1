package com.danilisan.kmp

import com.danilisan.kmp.domain.entity.BoardPosition
import com.danilisan.kmp.domain.entity.NumberBox
import com.danilisan.kmp.domain.entity.Score

object Mocks {
    //TODO Crear mocks para cada modo de juego
    val easyAddBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.RegularBox(1),
        BoardPosition(0, 1) to NumberBox.RegularBox(6),
        BoardPosition(0, 2) to NumberBox.RegularBox(2),
        BoardPosition(1, 0) to NumberBox.RegularBox(1),
        BoardPosition(1, 1) to NumberBox.RegularBox(0),
        BoardPosition(1, 2) to NumberBox.RegularBox(7),
        BoardPosition(2, 0) to NumberBox.RegularBox(3),
        BoardPosition(2, 1) to NumberBox.RegularBox(4),
        BoardPosition(2, 2) to NumberBox.RegularBox(7),
    )

    val hardMultiplyBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.RegularBox(1),
        BoardPosition(0, 1) to NumberBox.RegularBox(5),
        BoardPosition(0, 2) to NumberBox.RegularBox(6),
        BoardPosition(1, 0) to NumberBox.RegularBox(6),
        BoardPosition(1, 1) to NumberBox.RegularBox(5),
        BoardPosition(1, 2) to NumberBox.RegularBox(0),
        BoardPosition(2, 0) to NumberBox.RegularBox(4),
        BoardPosition(2, 1) to NumberBox.RegularBox(9),
        BoardPosition(2, 2) to NumberBox.RegularBox(1),
    )

    val jumboBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.RegularBox(0),
        BoardPosition(0, 1) to NumberBox.RegularBox(0),
        BoardPosition(0, 2) to NumberBox.RegularBox(1),
        BoardPosition(0, 3) to NumberBox.RegularBox(1),
        BoardPosition(1, 0) to NumberBox.RegularBox(1),
        BoardPosition(1, 1) to NumberBox.RegularBox(2),
        BoardPosition(1, 2) to NumberBox.RegularBox(7),
        BoardPosition(1, 3) to NumberBox.RegularBox(1),
        BoardPosition(2, 0) to NumberBox.RegularBox(2),
        BoardPosition(2, 1) to NumberBox.RegularBox(7),
        BoardPosition(2, 2) to NumberBox.RegularBox(4),
        BoardPosition(2, 3) to NumberBox.RegularBox(1),
    )


    val titleBoard:Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.RegularBox(103),
        BoardPosition(0, 1) to NumberBox.SilverStarBox(),
        BoardPosition(0, 2) to NumberBox.EmptyBox(),
        BoardPosition(1, 0) to NumberBox.EmptyBox(),
        BoardPosition(1, 1) to NumberBox.RegularBox(111),
        BoardPosition(1, 2) to NumberBox.EmptyBox(),
        BoardPosition(2, 0) to NumberBox.EmptyBox(),
        BoardPosition(2, 1) to NumberBox.RegularBox(113),
        BoardPosition(2, 2) to NumberBox.EmptyBox(),
    )

    val bingoNoStarsBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.RegularBox(6),
        BoardPosition(0, 1) to NumberBox.RegularBox(7),
        BoardPosition(0, 2) to NumberBox.RegularBox(4),
        BoardPosition(1, 0) to NumberBox.RegularBox(7),
        BoardPosition(1, 1) to NumberBox.BlockBox(9),
        BoardPosition(1, 2) to NumberBox.RegularBox(1),
        BoardPosition(2, 0) to NumberBox.RegularBox(4),
        BoardPosition(2, 1) to NumberBox.RegularBox(1),
        BoardPosition(2, 2) to NumberBox.RegularBox(2),
    )

    val bingoGoldenBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.GoldenStarBox(),
        BoardPosition(0, 1) to NumberBox.RegularBox(2),
        BoardPosition(0, 2) to NumberBox.RegularBox(3),
        BoardPosition(1, 0) to NumberBox.RegularBox(4),
        BoardPosition(1, 1) to NumberBox.GoldenStarBox(),
        BoardPosition(1, 2) to NumberBox.RegularBox(6),
        BoardPosition(2, 0) to NumberBox.RegularBox(7),
        BoardPosition(2, 1) to NumberBox.RegularBox(8),
        BoardPosition(2, 2) to NumberBox.GoldenStarBox(),
    )

    val bingoSilverBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.RegularBox(6),
        BoardPosition(0, 1) to NumberBox.RegularBox(7),
        BoardPosition(0, 2) to NumberBox.RegularBox(4),
        BoardPosition(1, 0) to NumberBox.RegularBox(7),
        BoardPosition(1, 1) to NumberBox.SilverStarBox(),
        BoardPosition(1, 2) to NumberBox.RegularBox(1),
        BoardPosition(2, 0) to NumberBox.RegularBox(4),
        BoardPosition(2, 1) to NumberBox.RegularBox(1),
        BoardPosition(2, 2) to NumberBox.RegularBox(2),
    )

    val regularNoStarsBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.RegularBox(6),
        BoardPosition(0, 1) to NumberBox.RegularBox(7),
        BoardPosition(0, 2) to NumberBox.RegularBox(4),
        BoardPosition(1, 0) to NumberBox.RegularBox(7),
        BoardPosition(1, 1) to NumberBox.BlockBox(8),
        BoardPosition(1, 2) to NumberBox.RegularBox(1),
        BoardPosition(2, 0) to NumberBox.RegularBox(4),
        BoardPosition(2, 1) to NumberBox.RegularBox(1),
        BoardPosition(2, 2) to NumberBox.RegularBox(2),
    )

    val regularSilverBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.SilverStarBox(),
        BoardPosition(0, 1) to NumberBox.RegularBox(2),
        BoardPosition(0, 2) to NumberBox.RegularBox(3),
        BoardPosition(1, 0) to NumberBox.RegularBox(4),
        BoardPosition(1, 1) to NumberBox.SilverStarBox(),
        BoardPosition(1, 2) to NumberBox.RegularBox(6),
        BoardPosition(2, 0) to NumberBox.RegularBox(7),
        BoardPosition(2, 1) to NumberBox.RegularBox(8),
        BoardPosition(2, 2) to NumberBox.SilverStarBox(),
    )

    val threeSilverStarsBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 2) to NumberBox.GoldenStarBox(),
        BoardPosition(0, 1) to NumberBox.RegularBox(1),
        BoardPosition(0, 0) to NumberBox.RegularBox(3),
        BoardPosition(1, 1) to NumberBox.SilverStarBox(),
        BoardPosition(1, 0) to NumberBox.RegularBox(5),
        BoardPosition(1, 2) to NumberBox.RegularBox(6),
        BoardPosition(2, 2) to NumberBox.SilverStarBox(),
        BoardPosition(2, 1) to NumberBox.RegularBox(9),
        BoardPosition(2, 0) to NumberBox.RegularBox(8),
    )

    val regularGoldenBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.GoldenStarBox(),
        BoardPosition(0, 1) to NumberBox.RegularBox(2),
        BoardPosition(0, 2) to NumberBox.RegularBox(3),
        BoardPosition(1, 0) to NumberBox.RegularBox(4),
        BoardPosition(1, 1) to NumberBox.RegularBox(5),
        BoardPosition(1, 2) to NumberBox.RegularBox(6),
        BoardPosition(2, 0) to NumberBox.RegularBox(7),
        BoardPosition(2, 1) to NumberBox.RegularBox(8),
        BoardPosition(2, 2) to NumberBox.GoldenStarBox(),
    )

    val blockedBoard: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to NumberBox.BlockBox(9),
        BoardPosition(0, 1) to NumberBox.RegularBox(0),
        BoardPosition(0, 2) to NumberBox.RegularBox(4),
        BoardPosition(1, 0) to NumberBox.RegularBox(4),
        BoardPosition(1, 1) to NumberBox.BlockBox(8),
        BoardPosition(1, 2) to NumberBox.RegularBox(6),
        BoardPosition(2, 0) to NumberBox.BlockBox(8),
        BoardPosition(2, 1) to NumberBox.BlockBox(9),
        BoardPosition(2, 2) to NumberBox.RegularBox(2),
    )

    private val pool = (0..9).toList()
    private fun randomNumberBox(): NumberBox = NumberBox.RegularBox(pool.random())

    val randomBoard3: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to randomNumberBox(),
        BoardPosition(0, 1) to randomNumberBox(),
        BoardPosition(0, 2) to randomNumberBox(),
        BoardPosition(1, 0) to randomNumberBox(),
        BoardPosition(1, 1) to randomNumberBox(),
        BoardPosition(1, 2) to randomNumberBox(),
        BoardPosition(2, 0) to randomNumberBox(),
        BoardPosition(2, 1) to randomNumberBox(),
        BoardPosition(2, 2) to randomNumberBox(),
    )

    val randomBoard4: Map<BoardPosition, NumberBox> = mapOf(
        BoardPosition(0, 0) to randomNumberBox(),
        BoardPosition(0, 1) to randomNumberBox(),
        BoardPosition(0, 2) to randomNumberBox(),
        BoardPosition(0, 3) to randomNumberBox(),
        BoardPosition(1, 0) to randomNumberBox(),
        BoardPosition(1, 1) to randomNumberBox(),
        BoardPosition(1, 2) to randomNumberBox(),
        BoardPosition(1, 3) to randomNumberBox(),
        BoardPosition(2, 0) to randomNumberBox(),
        BoardPosition(2, 1) to randomNumberBox(),
        BoardPosition(2, 2) to randomNumberBox(),
        BoardPosition(2, 3) to randomNumberBox(),
    )


    val allLines = setOf(0, 10, 20, 21, 22, 30, 31, 32)
    val someLines = setOf(0, 20, 21)
    val noLines = setOf<Int>()
    val lines4 = setOf(0, 20, 22, 23, 31, 33)

    val queue: List<NumberBox> = listOf(
        NumberBox.RegularBox(1),
        NumberBox.RegularBox(2),
        NumberBox.RegularBox(3),
    )

    val queue4: List<NumberBox> = listOf(
        NumberBox.RegularBox(1),
        NumberBox.RegularBox(2),
        NumberBox.RegularBox(3),
        NumberBox.RegularBox(4),
    )

    val mixedQueue: List<NumberBox> = listOf(
        NumberBox.SilverStarBox(),
        NumberBox.RegularBox(5),
        NumberBox.BlockBox(9)
    )

    val randomQueue: List<NumberBox> = listOf(
        randomNumberBox(), randomNumberBox(), randomNumberBox()
    )

    val score: Score = Score(
        points = 145231L,
        lines = 12,
        turns = 2,
    )

    val maxScore: Score = Score(
        points = 999_999_999_999L,
        lines = 99,
        turns = 99,
    )

    val overScore: Score = Score(
        points = 14599929392193L,
        lines = 2012,
        turns = 1202,
    )


}