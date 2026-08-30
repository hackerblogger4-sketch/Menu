package uz.lagan.receiver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val RESTAURANT = "lagan"
class MainActivity:ComponentActivity(){ override fun onCreate(b:Bundle?){super.onCreate(b);setContent{LaganApp()}} }
@Composable fun LaganApp(){
 val api=remember { Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(Api::class.java) }
 val scope=rememberCoroutineScope(); var token by remember { mutableStateOf<String?>(null) }; var pinDialog by remember { mutableStateOf(false) }; var adminDialog by remember { mutableStateOf(false) }
 var tables by remember { mutableStateOf<List<Table>>(emptyList()) }; var selected by remember { mutableStateOf<Table?>(null) }; var error by remember { mutableStateOf<String?>(null) }
 fun refresh(){scope.launch {runCatching{api.tables(RESTAURANT)}.onSuccess{tables=it}.onFailure{error="Serverga ulanib bo‘lmadi"}}}
 LaunchedEffect(Unit){while(true){refresh();delay(5000)}}
 Scaffold(floatingActionButton={FloatingActionButton(onClick={if(token==null)pinDialog=true else adminDialog=true}){Text("⚙")}}){pad->Column(Modifier.padding(pad).padding(16.dp)){
  Text("Lagan — Buyurtmalar",style=MaterialTheme.typography.headlineSmall); Text("🟢 Bo‘sh   🟠 Yangi   🔵 Ko‘rilgan")
  LazyVerticalGrid(columns=GridCells.Fixed(3),modifier=Modifier.fillMaxSize().padding(top=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){items(tables){t->TableCard(t){ if(token==null) {error="Buyurtmani ko‘rish uchun Admin PIN kiriting";pinDialog=true}else selected=t }}}
 }}
 if(pinDialog) PinDialog(onDismiss={pinDialog=false},onLogin={pin->scope.launch {runCatching{api.login(Login(RESTAURANT,pin)).token}.onSuccess{token=it;pinDialog=false;adminDialog=true;error=null}.onFailure{error="PIN noto‘g‘ri"}}})
 if(adminDialog && token!=null) AdminDialog(tables,token!!,api,{adminDialog=false},{refresh()})
 selected?.let {t-> OrderDialog(t,token!!,api,{selected=null},{refresh();selected=null})}
 error?.let{Snackbar(Modifier.padding(12.dp)){Text(it)}}
}
@Composable fun TableCard(t:Table,onClick:()->Unit){val c=when(t.status){"new"->Color(0xffffa726);"seen"->Color(0xff42a5f5);else->Color(0xff66bb6a)};Card(Modifier.height(110.dp).clickable{onClick()}){Column(Modifier.fillMaxSize().background(c).padding(10.dp),verticalArrangement=Arrangement.Center){Text("STOL ${t.number}",fontWeight=FontWeight.Bold);Text(if(t.status=="new")"YANGI" else if(t.status=="seen")"KO‘RILDI" else "BO‘SH")}}}
@Composable fun PinDialog(onDismiss:()->Unit,onLogin:(String)->Unit){var pin by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onDismiss,title={Text("ADMIN PANEL")},text={OutlinedTextField(pin,{pin=it},label={Text("PIN kod")})},confirmButton={Button(onClick={onLogin(pin)},enabled=pin.length>=4){Text("Kirish")}},dismissButton={TextButton(onClick=onDismiss){Text("Bekor qilish")}})}
@Composable fun AdminDialog(tables:List<Table>,token:String,api:Api,onDismiss:()->Unit,onChanged:()->Unit){val scope=rememberCoroutineScope();var number by remember{mutableStateOf("")};var info by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onDismiss,title={Text("ADMIN PANEL")},text={Column{Text("Stol qo‘shish va tartibini o‘zgartirish") ;OutlinedTextField(number,{number=it},label={Text("Yangi stol raqami")});Button(onClick={scope.launch{runCatching{api.addTable("Bearer $token",NewTable(number.toInt()))}.onSuccess{number="";info="Stol qo‘shildi";onChanged()}.onFailure{info="Stol qo‘shilmadi"}}}){Text("Qo‘shish")};Text(info);tables.sortedBy{it.sort_order}.forEachIndexed{i,t->Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Stol ${t.number}",Modifier.weight(1f));TextButton(enabled=i>0,onClick={scope.launch{api.updateTable(t.id,"Bearer $token",UpdateTable(i));api.updateTable(tables[i-1].id,"Bearer $token",UpdateTable(i+1));onChanged()}}){Text("↑")}}}}},confirmButton={Button(onClick=onDismiss){Text("Tayyor")}})}
@Composable fun OrderDialog(table:Table,token:String,api:Api,onDismiss:()->Unit,onDone:()->Unit){val scope=rememberCoroutineScope();var orders by remember{mutableStateOf<List<Order>>(emptyList())};LaunchedEffect(table.id){orders=api.orders(table.id,"Bearer $token");orders.filter{it.status=="new"}.forEach{api.seen(it.id,"Bearer $token")}};AlertDialog(onDismissRequest=onDismiss,title={Text("STOL ${table.number}")},text={Column{if(orders.isEmpty())Text("Faol buyurtma yo‘q");orders.forEach{o->o.items.forEach{i->Text("${i.quantity} × ${i.name} — ${i.price*i.quantity} so‘m")};o.customer_note?.takeIf{it.isNotBlank()}?.let{Text("Izoh: $it")};HorizontalDivider()}}},confirmButton={Button(onClick={onDone}){Text("Yopish")}})}
