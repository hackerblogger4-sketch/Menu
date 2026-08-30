package uz.lagan.receiver

import retrofit2.http.*

data class Table(val id:Int,val number:Int,val sort_order:Int,val status:String)
data class OrderItem(val name:String,val price:Int,val quantity:Int)
data class Order(val id:Int,val status:String,val customer_note:String?,val created_at:String,val items:List<OrderItem>)
data class Login(val slug:String,val pin:String)
data class Token(val token:String)
data class NewTable(val number:Int)
data class UpdateTable(val sortOrder:Int)
interface Api {
 @POST("api/auth/pin") suspend fun login(@Body input:Login):Token
 @GET("api/restaurants/{slug}/tables") suspend fun tables(@Path("slug") slug:String):List<Table>
 @GET("api/tables/{id}/orders/active") suspend fun orders(@Path("id") id:Int,@Header("Authorization") token:String):List<Order>
 @POST("api/orders/{id}/seen") suspend fun seen(@Path("id") id:Int,@Header("Authorization") token:String):Any
 @POST("api/admin/tables") suspend fun addTable(@Header("Authorization") token:String,@Body table:NewTable):Any
 @PATCH("api/admin/tables/{id}") suspend fun updateTable(@Path("id") id:Int,@Header("Authorization") token:String,@Body table:UpdateTable):Any
}
