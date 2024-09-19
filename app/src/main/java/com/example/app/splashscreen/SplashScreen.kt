package com.example.app.splashscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.R

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.light)),
        contentAlignment = Alignment.Center
    ) {
        // Centered Icon or App Logo
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img3 ), // Replace with your icon resource
                contentDescription = "App Icon",
                modifier = Modifier.size(200 .dp) // Adjust the size as needed
            )
            Spacer(modifier = Modifier.padding(top = 48.dp))
        }
        // Email and Contact details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Rectangle image at the bottom
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, start = 16.dp, bottom = 0.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img2),
                    contentDescription = "Rectangle Image",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(62.dp)

                )

            }


            Text(
                text = "Address: 2A/638,Krishna Vihar, BSA engg.College, Mathura- 281001 (UP)",
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 6.dp, end = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Contact: +91 82184 88284",
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    SplashScreen()
}

