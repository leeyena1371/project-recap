package co.project.api.common.exception;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 오류
 */
@RestControllerAdvice(annotations = {RestController.class, Controller.class})
public class CommonException extends Exception {


    public static class InternalException extends Exception{
        public InternalException(String message){super(message);}
    }

    public static class DefaultParameterException extends Exception {
        public DefaultParameterException(String message) {
            super(message);
        }
    }

    public static class RdsExecutionException extends Exception{
        public RdsExecutionException(String message){super(message);}
    }

    public static class RedisExecutionException extends Exception{
        public RedisExecutionException(String message){super(message);}
    }

    public static class IotExecutionException extends Exception{
        public IotExecutionException(String message){super(message);}
    }
}
