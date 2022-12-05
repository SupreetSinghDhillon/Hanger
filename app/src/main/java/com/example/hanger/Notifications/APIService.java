package com.example.hanger.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAm_K8PXI:APA91bGaFvrNuOke5xrpPXLP8a77esGQvVCXAHH4Z1A0DHR7Hdky31Jy--_g6vOWkwdMVhTYi3EJ3QGNlvJVq4X_wkF9DuvwJWFBV1Pn7NbZAACenBVJBSPOwUX6sjn8X850lKzUpQJw"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);


}
