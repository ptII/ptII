package ptolemy.actor.ptalon.model.controls;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LabelledItemPanel extends JPanel
{
    /** The row to add the next labelled item to */
    private int myNextItemRow = 0;

    public LabelledItemPanel()
    {
        init();
    }

    private void init()
    {
        setLayout(new GridBagLayout());

        // Create a blank label to use as a vertical fill so that the
        // label/item pairs are aligned to the top of the panel and are not
        // grouped in the centre if the parent component is taller than
        // the preferred size of the panel.

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx   = 0;
        constraints.gridy   = 99;
        constraints.insets  = new Insets(10, 0, 0, 0);
        constraints.weighty = 1.0;
        constraints.fill    = GridBagConstraints.VERTICAL;

        JLabel verticalFillLabel = new JLabel();

        add(verticalFillLabel, constraints);
    }

    public void addItem(String labelText, JComponent item)
    {
        // Create the label and its constraints

        JLabel label = new JLabel(labelText);

        GridBagConstraints labelConstraints = new GridBagConstraints();

        labelConstraints.gridx   = 0;
        labelConstraints.gridy   = myNextItemRow;
        labelConstraints.insets  = new Insets(10, 10, 0, 0);
        labelConstraints.anchor  = GridBagConstraints.NORTHWEST;
        labelConstraints.fill    = GridBagConstraints.NONE;

        add(label, labelConstraints);

        // Add the component with its constraints

        GridBagConstraints itemConstraints = new GridBagConstraints();

        itemConstraints.gridx   = 1;
        itemConstraints.gridy   = myNextItemRow;
        itemConstraints.insets  = new Insets(10, 10, 0, 10);
        itemConstraints.weightx = 1.0;
        itemConstraints.anchor  = GridBagConstraints.WEST;
        itemConstraints.fill    = GridBagConstraints.HORIZONTAL;

        add(item, itemConstraints);

        myNextItemRow++;
    }
}
