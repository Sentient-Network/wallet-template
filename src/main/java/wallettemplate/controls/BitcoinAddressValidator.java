package wallettemplate.controls;

import com.netki.WalletNameResolver;
import com.netki.exceptions.WalletNameLookupException;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import wallettemplate.utils.TextFieldValidator;

import java.net.IDN;
import java.util.regex.Pattern;

/**
 * Given a text field, some network params and optionally some nodes, will make the text field an angry red colour
 * if the address is invalid for those params, and enable/disable the nodes.
 */
public class BitcoinAddressValidator {
    private NetworkParameters params;
    private Node[] nodes;

    // WalletName Validation
    private WalletNameResolver walletNameResolver;
    private static Pattern pDomainName;
    private static final String DOMAIN_NAME_PATTERN = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,16}$";

    static {
        pDomainName = Pattern.compile(DOMAIN_NAME_PATTERN);
    }

    public BitcoinAddressValidator(NetworkParameters params, TextField field, Node... nodes) {
        this.params = params;
        this.nodes = nodes;
        this.walletNameResolver = new WalletNameResolver();

        // Handle the red highlighting, but don't highlight in red just when the field is empty because that makes
        // the example/prompt address hard to read.
        new TextFieldValidator(field, text -> text.isEmpty() || testAddr(text));
        // However we do want the buttons to be disabled when empty so we apply a different test there.
        field.textProperty().addListener((observableValue, prev, current) -> {
            toggleButtons(current);
        });
        toggleButtons(field.getText());
    }

    private void toggleButtons(String current) {
        boolean valid = testAddr(current);
        for (Node n : nodes) n.setDisable(!valid);
    }

    private boolean testWalletName(String text) {
        return pDomainName.matcher(IDN.toASCII(text)).find();
    }

    private boolean testAddr(String text) {
        try {
            new Address(params, text);
            return true;
        } catch (AddressFormatException e) {
            try {
                if (!this.testWalletName(text)) return false;
                this.walletNameResolver.resolve(text, "btc", false);
                return true;
            } catch (WalletNameLookupException ignored) {

            }
            return false;
        }
    }
}
