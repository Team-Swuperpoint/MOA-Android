package com.swuperpoint.moa_android.widget.utils

import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.swuperpoint.moa_android.R

/* Compose를 활용한 커스텀 Shadow 파일 */
// Compose - 커스텀 inner shadow
fun Modifier.innerShadow(
    shape: Shape,
    color: Color,
    blur: Dp,
    offsetY: Dp,
    offsetX: Dp,
    spread: Dp
) = drawWithContent {
    drawContent()

    val rect = Rect(Offset.Zero, size)
    val paint = Paint().apply {
        this.color = color
        this.isAntiAlias = true
    }

    val shadowOutline = shape.createOutline(size, layoutDirection, this)

    drawIntoCanvas { canvas ->
        canvas.saveLayer(rect, paint)
        canvas.drawOutline(shadowOutline, paint)

        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        if (blur.toPx() > 0) {
            frameworkPaint.maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        paint.color = Color.Black

        val spreadOffsetX = offsetX.toPx() + if (offsetX.toPx() < 0) -spread.toPx() else spread.toPx()
        val spreadOffsetY = offsetY.toPx() + if (offsetY.toPx() < 0) -spread.toPx() else spread.toPx()

        canvas.translate(spreadOffsetX, spreadOffsetY)
        canvas.drawOutline(shadowOutline, paint)
        canvas.restore()
    }
}

// 홈 화면의 왼쪽,오른쪽 이동 버튼 그림자
@Composable
fun HomeButtonInnerShadow() {
    val customColor = Color(0x66A3A3C0)
    val customColor2 = Color(0x66FFFFFF)

    Modifier.innerShadow(
        shape = CircleShape,
        color = customColor,
        blur = 3.dp,
        offsetX = 2.dp,
        offsetY = 2.dp,
        spread = 0.dp
    )
    Modifier.innerShadow(
        shape = CircleShape,
        color = customColor2,
        blur = 3.dp,
        offsetX = 2.dp,
        offsetY = 2.dp,
        spread = 0.dp
    )
}

// Int -> Color 변환 함수
@Composable
fun getColorFromResource(color: Int): Color {
    val context = LocalContext.current
    val colorInt = ContextCompat.getColor(context, color)
    return Color(colorInt)
}

@Preview(apiLevel = 33)
@Composable
fun PreviewHomeInnerShadow() {
    HomeButtonInnerShadow()
}