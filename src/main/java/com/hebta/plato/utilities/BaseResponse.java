package com.hebta.plato.utilities;

public class BaseResponse<T> {
    private boolean success  = false;
    private String msg = "message not set";
    private T data;
	private String code;
	public BaseResponse(RESPONSE_STATUS status) {
        switch (status) {
            case SUCCESS:
                success = true;
                break;
            case FAIL:
                success =false;
                break;
        }
    }
	
    public static enum RESPONSE_STATUS {
        SUCCESS ("success"),
        FAIL ("fail");
        private String code;
        private RESPONSE_STATUS(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    };
    
    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
	public void setStatus(RESPONSE_STATUS status) {
        switch (status) {
            case SUCCESS:
                success = true;
                break;
            case FAIL:
                success =false;
                break;
        }
    }

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	 public String getCode() {
			return code;
		}

	public void setCode(String code) {
		this.code=code;
	}	
}
