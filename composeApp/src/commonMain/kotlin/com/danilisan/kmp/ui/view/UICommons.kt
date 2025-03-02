package com.danilisan.kmp.ui.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UISpacer(
    size: Int = 20,
    horizontal: Boolean = true
) {
    Spacer(
        modifier = if (horizontal) {
            Modifier.height(size.dp)
        } else {
            Modifier.width(size.dp)
        }
    )
}

/*
@Composable
fun ReloadButton(onClickAction: () -> Unit){
    Button(onClick = onClickAction){
        Text(
            text = stringResource(
                resource = Res.string.reloadBtn,
                stringResource(Res.string.boardTag),
                "2")
        )
    }
}

@Composable
fun ReloadButton2(onClickAction: () -> Unit){
    Button(onClick = onClickAction){
        Text(
            text = stringResource(
                resource = Res.string.reloadBtn,
                stringResource(Res.string.queueTag),
                "1")
        )
    }
}

@Composable
fun Board(
    boardNumbers: Set<BoardNumberBox>,
    lineLength: Int){
    Column(verticalArrangement = Arrangement.Center) {
        for (row in 0..<lineLength) {
            Row(horizontalArrangement = Arrangement.Center) {
                boardNumbers
                    .filter { it.position.row == row }
                    .sortedBy {it.position.column}
                    .map {
                        Text(
                            text = it.number.value.toString(),
                            modifier =  Modifier.padding(16.dp)
                        )
                    }
            }
        }
    }
}

@Composable
fun Queue(
    queueNumbers: List<NumberBox>,
){
    Column(verticalArrangement = Arrangement.Center){
        Row(horizontalArrangement = Arrangement.Center){
            for(i in queueNumbers.indices){
                val fontSize = 16 - i
                val padding = 8 + i
                Text(
                    text = queueNumbers[i].value.toString(),
                    fontSize = fontSize.sp,
                    modifier = Modifier.padding(padding.dp)
                )
            }
        }
    }
}


@Composable
fun LoadingText(isLoading: Boolean){
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()){
        Text(text = if(isLoading){
            "Cargando..."
        }else{
            "Cargado"
        })

    }
}

@Composable
fun DemoKMP(){
    var showContent by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { showContent = !showContent }) {
            Text("Click me!")
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                //Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
    }
}
*/