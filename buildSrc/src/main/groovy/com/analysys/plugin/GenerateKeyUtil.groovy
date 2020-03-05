import com.analysys.plugin.StringFog
import org.gradle.api.Project
import org.json.simple.JSONObject

class GenerateKeyUtil {
    public static void generateKey(Project project) {
// 访问 STRING_FOG_KEY

        File file = new File(project.projectDir, "build.gradle")
        FileInputStream inputStream = new FileInputStream(file)
        InputStreamReader isr = new InputStreamReader(inputStream)
        BufferedReader reader = new BufferedReader(isr)
        String key = "aaaa";
        while (true) {
            String line = reader.readLine()
            if (line == null) {
                println("error: STRING_FOG_KEY not found !")
                break
            }
            if (line.startsWith("def STRING_FOG_KEY")) {
                String[] keys = line.split("\"")
                if (keys.length < 2) {
                    println("error: STRING_FOG_KEY not found !")
                }
                key = keys[keys.length - 1]
                println("STRING_FOG_KEY = [" + key + "]")
                break
            }
        }
        reader.close()


        // 访问 StringFogImpl 里面的 map
        StringFog.StringFogImpl sfi = new StringFog.StringFogImpl()
        // 生成 ps
        String ps = generatePs(key, sfi.hset)
        println("ps = " + ps);
        // 替换 ps

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(project.projectDir, ".\\src\\main\\java\\com\\analysys\\plugin\\Key.java"))))

        writer.write("package com.analysys.plugin;\n" +
                "\n" +
                "public class Key {" + ps + "}")
        writer.flush()
        writer.close()
    }

    public static String generatePs(String key, Map<String, String> hset) {
        String ps = null;
        try {
            JSONObject obj = new JSONObject();
            for (String item : hset.keySet()
            ) {
                obj.put(hset.get(item), item);
            }

            String s = obj.toString()

            println(s)
            byte[] data = s.getBytes("utf-8");

            int len = data.length;
            int lenKey = key.length();
            int i = 0;
            int j = 0;

            while (i < len) {
                if (j >= lenKey) {
                    j = 0;
                }
                int c = (int) key.charAt(j);
                data[i] = (byte) (data[i] ^ c);
                i++;
                j++;
            }

            String psb = Arrays.toString(data)

            ps = "public static final byte[] bs= new byte[]{" + psb.substring(1, psb.length() - 1) + "};";
            return ps;
        } catch (Throwable e) {
            println(e)
        }
    }

}
