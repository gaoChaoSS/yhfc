package net.shopxx.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 	保留小数点后0位
 * @author yangli
 *
 */
public class BigDecimalSerialize_0 extends JsonSerializer<BigDecimal>{
	
	private DecimalFormat df = new DecimalFormat("0.00");

	@Override
	public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		 if(value != null) {
	            df.setMaximumFractionDigits(0);//显示几位修改几
	            df.setGroupingSize(0);
	            df.setRoundingMode(RoundingMode.FLOOR);
	            //根据实际情况选择使用
	            // gen.writeString(df.format(value));  // 返回出去是字符串
	            gen.writeNumber(df.format(value));  // 返回出去是数字形式
	 
	        }
	}

}
