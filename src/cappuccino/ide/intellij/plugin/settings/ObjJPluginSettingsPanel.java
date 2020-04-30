package cappuccino.ide.intellij.plugin.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

public class ObjJPluginSettingsPanel {
    public JPanel settingsPanel;
    public JCheckBox underscore_ignoreClassesCheckbox;
    public JCheckBox unqualifiedIgnore_ignoreMethodDec;
    public JCheckBox unqualifiedIgnore_ignoreUndecVars;
    public JCheckBox unqualifiedIgnore_ignoreConflictingMethodDecs;
    public JCheckBox unqualifiedIgnore_ignoreMethodReturnErrors;
    public JCheckBox unqualifiedIgnore_ignoreInvalidSelectors;
    public JTextArea globallyIgnoredVariableNames;
    public JTextArea globallyIgnoredSelectors;
    public JCheckBox resolveVariableTypeFromAssignments;
    public JCheckBox filterMethodCallStrictIfTypeKnown;
    public JTextArea globallyIgnoredFunctionNames;
    public JTextArea globallyIgnoredClassNames;
    public JCheckBox ignoreMissingClassesWhenSuffixedWithRefOrPointer;
    public JCheckBox inferMethodCallReturnTypes;
    public JCheckBox inferFunctionReturnType;
    public JCheckBox minimizeJumps;

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayoutManager(32, 1, new Insets(0, 0, 0, 0), - 1, - 1));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, - 1, 16, label1.getFont());
        if (label1Font != null) {
            label1.setFont(label1Font);
        }
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.classes.underscore.title"));
        label1.setVerticalAlignment(1);
        settingsPanel.add(label1, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        underscore_ignoreClassesCheckbox = new JCheckBox();
        this.$$$loadButtonText$$$(underscore_ignoreClassesCheckbox, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.classes.underscore.checkbox"));
        settingsPanel.add(underscore_ignoreClassesCheckbox, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        settingsPanel.add(separator1, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(- 1, 4), new Dimension(- 1, 4), 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, - 1, 16, label2.getFont());
        if (label2Font != null) {
            label2.setFont(label2Font);
        }
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.ignore.unqualified.title"));
        settingsPanel.add(label2, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.ignore.unqualified.description"));
        settingsPanel.add(label3, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unqualifiedIgnore_ignoreMethodDec = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreMethodDec, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.ignore.unqualified.option.removeMethod"));
        settingsPanel.add(unqualifiedIgnore_ignoreMethodDec, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        unqualifiedIgnore_ignoreUndecVars = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreUndecVars, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.ignore.unqualified.option.undeclaredVariables"));
        settingsPanel.add(unqualifiedIgnore_ignoreUndecVars, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        unqualifiedIgnore_ignoreConflictingMethodDecs = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreConflictingMethodDecs, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.ignore.unqualified.option.conflictingMethodDecs"));
        settingsPanel.add(unqualifiedIgnore_ignoreConflictingMethodDecs, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        unqualifiedIgnore_ignoreMethodReturnErrors = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreMethodReturnErrors, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.ignore.unqualified.option.methodReturnValueErrors"));
        settingsPanel.add(unqualifiedIgnore_ignoreMethodReturnErrors, new GridConstraints(16, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        unqualifiedIgnore_ignoreInvalidSelectors = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreInvalidSelectors, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.ignore.unqualified.option.invalidSelectors"));
        settingsPanel.add(unqualifiedIgnore_ignoreInvalidSelectors, new GridConstraints(17, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JSeparator separator2 = new JSeparator();
        settingsPanel.add(separator2, new GridConstraints(18, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(- 1, 4), new Dimension(- 1, 4), 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, - 1, 16, label4.getFont());
        if (label4Font != null) {
            label4.setFont(label4Font);
        }
        this.$$$loadLabelText$$$(label4, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.title"));
        settingsPanel.add(label4, new GridConstraints(19, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.variableNames.title"));
        settingsPanel.add(label5, new GridConstraints(20, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$(null, - 1, 11, label6.getFont());
        if (label6Font != null) {
            label6.setFont(label6Font);
        }
        this.$$$loadLabelText$$$(label6, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.variableNames.hint"));
        settingsPanel.add(label6, new GridConstraints(21, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        globallyIgnoredVariableNames = new JTextArea();
        globallyIgnoredVariableNames.setLineWrap(true);
        settingsPanel.add(globallyIgnoredVariableNames, new GridConstraints(22, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label7 = new JLabel();
        this.$$$loadLabelText$$$(label7, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.methodSelectors.title"));
        settingsPanel.add(label7, new GridConstraints(23, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        Font label8Font = this.$$$getFont$$$(null, - 1, 11, label8.getFont());
        if (label8Font != null) {
            label8.setFont(label8Font);
        }
        this.$$$loadLabelText$$$(label8, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.methodSelectors.hint"));
        settingsPanel.add(label8, new GridConstraints(24, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        globallyIgnoredSelectors = new JTextArea();
        globallyIgnoredSelectors.setLineWrap(true);
        settingsPanel.add(globallyIgnoredSelectors, new GridConstraints(25, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label9 = new JLabel();
        Font label9Font = this.$$$getFont$$$(null, - 1, 16, label9.getFont());
        if (label9Font != null) {
            label9.setFont(label9Font);
        }
        this.$$$loadLabelText$$$(label9, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.resolve.title"));
        settingsPanel.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resolveVariableTypeFromAssignments = new JCheckBox();
        this.$$$loadButtonText$$$(resolveVariableTypeFromAssignments, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.resolve.calltarget.checkbox"));
        settingsPanel.add(resolveVariableTypeFromAssignments, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator3 = new JSeparator();
        settingsPanel.add(separator3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(- 1, 4), new Dimension(- 1, 4), 0, false));
        filterMethodCallStrictIfTypeKnown = new JCheckBox();
        filterMethodCallStrictIfTypeKnown.setSelected(false);
        this.$$$loadButtonText$$$(filterMethodCallStrictIfTypeKnown, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.resolve.filterTypeStrict.checkbox"));
        settingsPanel.add(filterMethodCallStrictIfTypeKnown, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setEnabled(true);
        Font label10Font = this.$$$getFont$$$(null, - 1, 13, label10.getFont());
        if (label10Font != null) {
            label10.setFont(label10Font);
        }
        this.$$$loadLabelText$$$(label10, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.function-names.title"));
        settingsPanel.add(label10, new GridConstraints(26, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        Font label11Font = this.$$$getFont$$$(null, - 1, 11, label11.getFont());
        if (label11Font != null) {
            label11.setFont(label11Font);
        }
        this.$$$loadLabelText$$$(label11, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.function-names.hint"));
        settingsPanel.add(label11, new GridConstraints(27, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        globallyIgnoredFunctionNames = new JTextArea();
        globallyIgnoredFunctionNames.setLineWrap(true);
        settingsPanel.add(globallyIgnoredFunctionNames, new GridConstraints(28, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label12 = new JLabel();
        this.$$$loadLabelText$$$(label12, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.class-names.title"));
        settingsPanel.add(label12, new GridConstraints(29, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        Font label13Font = this.$$$getFont$$$(null, - 1, 11, label13.getFont());
        if (label13Font != null) {
            label13.setFont(label13Font);
        }
        this.$$$loadLabelText$$$(label13, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.global-ignores.class-names.hint"));
        settingsPanel.add(label13, new GridConstraints(30, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        globallyIgnoredClassNames = new JTextArea();
        globallyIgnoredClassNames.setLineWrap(true);
        settingsPanel.add(globallyIgnoredClassNames, new GridConstraints(31, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        ignoreMissingClassesWhenSuffixedWithRefOrPointer = new JCheckBox();
        this.$$$loadButtonText$$$(ignoreMissingClassesWhenSuffixedWithRefOrPointer, this.$$$getMessageFromBundle$$$("cappuccino/ide/intellij/plugin/lang/objective-j-bundle", "objective-j.settings.classes.on-ref-or-pointer-suffix.checkbox"));
        settingsPanel.add(ignoreMissingClassesWhenSuffixedWithRefOrPointer, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        inferMethodCallReturnTypes = new JCheckBox();
        inferMethodCallReturnTypes.setText("Infer method call return type from expressions");
        settingsPanel.add(inferMethodCallReturnTypes, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        inferFunctionReturnType = new JCheckBox();
        inferFunctionReturnType.setText("Infer function return type from expressions");
        settingsPanel.add(inferFunctionReturnType, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        minimizeJumps = new JCheckBox();
        minimizeJumps.setText("Minimize inference jumps");
        settingsPanel.add(minimizeJumps, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) {
            return null;
        }
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = - 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) {
                    break;
                }
                if (! haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = - 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) {
                    break;
                }
                if (! haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return settingsPanel;
    }

}
