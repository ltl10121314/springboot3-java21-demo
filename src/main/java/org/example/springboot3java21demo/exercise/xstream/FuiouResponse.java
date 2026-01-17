package org.example.springboot3java21demo.exercise.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ap")
public class FuiouResponse {

    @XStreamAlias("signature")
    private String signature;
    @XStreamAlias("plain")
    private Plain plain;

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public Plain getPlain() {
        return plain;
    }

    public void setPlain(Plain plain) {
        this.plain = plain;
    }

    public class Plain {
        @XStreamAlias("resp_code")
        private String respCode;

        @XStreamAlias("resp_desc")
        private String respDesc;

        public String getRespCode() {
            return respCode;
        }

        public void setRespCode(String respCode) {
            this.respCode = respCode;
        }

        public String getRespDesc() {
            return respDesc;
        }

        public void setRespDesc(String respDesc) {
            this.respDesc = respDesc;
        }
    }
}
