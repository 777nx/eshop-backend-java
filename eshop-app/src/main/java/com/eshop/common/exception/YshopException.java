

//
//
//

//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
///**
// * 自定义异常
// * @author zhonghui
// * @date 2020-04-30
// */
//@Data
//@EqualsAndHashCode(callSuper = true)
//public class YshopException extends RuntimeException{
//
//    private static final long serialVersionUID = -2470461654663264392L;
//
//    private Integer errorCode;
//    private String message;
//
//    public YshopException() {
//        super();
//    }
//
//    public YshopException(String message) {
//        super(message);
//        this.message = message;
//    }
//
//    public YshopException(Integer errorCode, String message) {
//        super(message);
//        this.errorCode = errorCode;
//        this.message = message;
//    }
//
//    public YshopException(ApiCode apiCode) {
//        super(apiCode.getMessage());
//        this.errorCode = apiCode.getCode();
//        this.message = apiCode.getMessage();
//    }
//
//    public YshopException(String message, Throwable cause) {
//        super(message, cause);
//    }
//
//    public YshopException(Throwable cause) {
//        super(cause);
//    }
//
//}
