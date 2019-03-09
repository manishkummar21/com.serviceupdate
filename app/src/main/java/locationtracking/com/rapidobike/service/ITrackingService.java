package locationtracking.com.rapidobike.service;

import io.reactivex.Observable;
import locationtracking.com.rapidobike.model.Points;
import retrofit2.Response;
import retrofit2.http.GET;

public interface ITrackingService {

    @GET("explore")
    Observable<Response<Points>> getPoints();
}
