package Controllers;

import Models.MessageViewModel;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageCustomCellControllerTest {
    @Test
    public void reuseLoadedNodeOnUpdate() {
        new JFXPanel();
        MessageCustomCellController cell = new MessageCustomCellController();
        MessageViewModel m1 = new MessageViewModel("Hi", "10:00", true, false, null);
        MessageViewModel m2 = new MessageViewModel("Hello", "10:01", true, false, null);
        cell.updateItem(m1, false);
        Object first = cell.getGraphic();
        cell.updateItem(m2, false);
        Object second = cell.getGraphic();
        assertSame(first, second);
    }
}
