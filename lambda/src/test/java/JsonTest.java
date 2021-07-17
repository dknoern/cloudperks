import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

public class JsonTest {
    @Test
    void invokeTest() {

        JsonObject member = new JsonObject();
        member.addProperty("memberId","23423423");

        System.out.println(member.getAsJsonObject().toString());

        System.out.println("local date: " + DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()));
        System.out.println("local datetime: " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));

        assertEquals(45,45);
    }
}
