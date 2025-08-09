package Controllers;

import Models.UserViewModel;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserCustomCellControllerTest {
    @Test
    public void reuseLoadedNodeOnUpdate() {
        new JFXPanel();
        UserCustomCellController cell = new UserCustomCellController();
        UserViewModel u1 = new UserViewModel("A", "m1", "10:00", "0", null);
        UserViewModel u2 = new UserViewModel("B", "m2", "10:01", "0", null);
        cell.updateItem(u1, false);
        Object first = cell.getGraphic();
        cell.updateItem(u2, false);
        Object second = cell.getGraphic();
        assertSame(first, second);
    }
}
