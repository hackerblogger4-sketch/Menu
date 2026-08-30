plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace="uz.lagan.receiver"; compileSdk=35
 defaultConfig { applicationId="uz.lagan.receiver"; minSdk=26; targetSdk=35; versionCode=1; versionName="0.1.0"; buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("API_BASE_URL") ?: "http://10.0.2.2:3000/"}\"") }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildFeatures { compose=true; buildConfig=true }
 composeOptions { kotlinCompilerExtensionVersion="1.5.15" }
}
dependencies {
 implementation("androidx.core:core-ktx:1.15.0")
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation(platform("androidx.compose:compose-bom:2025.01.00"))
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("com.squareup.retrofit2:retrofit:2.11.0")
 implementation("com.squareup.retrofit2:converter-gson:2.11.0")
 implementation("io.socket:socket.io-client:2.1.1") { exclude(group="org.json", module="json") }
 debugImplementation("androidx.compose.ui:ui-tooling")
}
