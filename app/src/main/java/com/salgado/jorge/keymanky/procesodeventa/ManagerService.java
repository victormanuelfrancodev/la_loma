package com.salgado.jorge.keymanky.procesodeventa;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ManagerService {
    @POST("webresources/authenticate/login")
    @Headers({"Content-Type: application/json"})
    Call<ResponseBody> setConfirmar(@Body RequestBody requestBody);

    @POST("webresources/workplan/client/checkinClient")
    @Headers({"Content-Type: application/json"})
    Call<ResponseBody> setInsertar(@Header("api_key") String str, @Header("auth_token") String str2, @Body RequestBody requestBody);
}