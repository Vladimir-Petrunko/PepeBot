package pepe.horoscope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Horoscope {
    private String name;
    private String title;
    private String[] descriptions;
    private String photoPath;

    public String getRandomDescription() {
        int i = (int) (Math.random() * this.descriptions.length);
        return this.descriptions[i];
    }
}
