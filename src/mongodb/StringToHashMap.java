package mongodb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import twitter4j.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StringToHashMap {
	
	public HashMap<String, ?> StringToHashMap (String body) {
		HashMap<String, ?> hm = new HashMap();
		
		hm = getHashMap(getList(body));
		
		return hm;
	}
	
	private String getList (String string) {
		Map<String, String> hm = new HashMap<String, String>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			 
			hm = mapper.readValue(string, 
			    new TypeReference<HashMap<String,String>>(){});
	 
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("list" + hm.get("list"));
		
		
		return hm.get("list");
	}
	
	private HashMap<String, ?> getHashMap (String string) {
		Map<String, String> hm = new HashMap();
		HashMap returnMap = new HashMap();
		ObjectMapper mapper = new ObjectMapper();
		try {
			 
			hm = mapper.readValue(string, 
			    new TypeReference<HashMap<String,?>>(){});
	 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		JSONObject JSONBody = new JSONObject(string);		
//		Iterator<?> keys = JSONBody.keys();
//
//		while(keys.hasNext()){
//			String key = (String)keys.next();
//			String value = JSONBody.getString(key); 
//			body.put(key, value);
//		}		
		
		
//        Iterator<Map.Entry<String, String>> iterator = hm.entrySet().iterator() ;
//        while(iterator.hasNext()){
//            Map.Entry entry = iterator.next();
//            System.out.println(entry.getKey() +" :: "+ entry.getValue());
//            
//            if(entry.getValue().toString().contains("{")) {
//            	returnMap.put(entry.getKey(), (HashMap<String, String>) entry.getValue());
//            }
//            else {
//            	returnMap.put(entry.getKey(), entry.getValue());
//            }   
//        }
		
		return (HashMap<String, ?>) hm;
	}

}
