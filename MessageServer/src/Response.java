import org.json.simple.JSONObject;

public class Response {
    public JSONObject response;
    private String message;
    public Response(boolean Error,String ErrorMessage){
        if(Error)
        {
            message = ErrorMessage;
            response = CreateErrorResponse();
        }else{
            response = SuccessResponse();
        }
    }

    private JSONObject SuccessResponse()
    {
        JSONObject success = new JSONObject();
        success.put("_class","SuccessResponse");
        return success;
    }

    private JSONObject CreateErrorResponse(){
        JSONObject error = new JSONObject();
        error.put("_class","ErrorResponse");
        error.put("error",message);
        return error;
    }
}
