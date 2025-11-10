import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CalculatorApp extends JFrame {
    // Display + copy strip
    private final JTextField display = new JTextField("0");
    private final JLabel copyStrip = new JLabel("Copy", SwingConstants.CENTER);

    // State
    private BigDecimal left = BigDecimal.ZERO;
    private String pendingOp = null; // "+", "−", "×", "÷", "xʸ"
    private boolean enteringNew = true;
    private BigDecimal mem = BigDecimal.ZERO;

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    public CalculatorApp() {
        super("Scientific Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(950, 720));
        getRootPane().setBorder(new EmptyBorder(10,10,10,10));

        // Top mode row (Deg/Rad/Grad)
        ButtonGroup angGroup = new ButtonGroup();
        JToggleButton deg = bigToggle("Deg", true);
        JToggleButton rad = bigToggle("Rad", false);
        JToggleButton grad = bigToggle("Grad", false);
        angGroup.add(deg); angGroup.add(rad); angGroup.add(grad);

        // Display
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
        display.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Copy strip
        copyStrip.setOpaque(true);
        copyStrip.setBackground(new Color(255, 236, 179));
        copyStrip.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        copyStrip.setPreferredSize(new Dimension(100, 55));

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        tabs.addTab("Basic", buildBasic(deg, rad, grad));
        tabs.addTab("Scientific", buildScientific(deg, rad, grad));

        // Top bar container
        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.add(display, BorderLayout.NORTH);
        north.add(copyStrip, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // copy on click
        copyStrip.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                String s = display.getText();
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(s), null);
                copyStrip.setText("Copied");
                Timer t = new Timer(1200, ev -> copyStrip.setText("Copy"));
                t.setRepeats(false);
                t.start();
            }
        });

        setLocationRelativeTo(null);
    }

    private JPanel buildBasic(JToggleButton deg, JToggleButton rad, JToggleButton grad) {
        JPanel p = new JPanel(new BorderLayout(10,10));

        // main 5x5 like your screenshot
        JPanel grid = new JPanel(new GridLayout(5, 5, 10, 10));
        // Row1
        grid.add(bigBtn("C", e -> clearAll()));
        grid.add(bigBtn("Back", e -> backspace()));
        grid.add(bigBtn("CE", e -> clearEntry()));
        grid.add(bigBtn("%", e -> percent()));
        grid.add(opBtn("÷"));
        // Row2
        grid.add(bigBtn("7", e -> inputDigit("7")));
        grid.add(bigBtn("8", e -> inputDigit("8")));
        grid.add(bigBtn("9", e -> inputDigit("9")));
        grid.add(opBtn("×"));
        grid.add(bigBtn("1/x", e -> unaryReciprocal()));
        // Row3
        grid.add(bigBtn("4", e -> inputDigit("4")));
        grid.add(bigBtn("5", e -> inputDigit("5")));
        grid.add(bigBtn("6", e -> inputDigit("6")));
        grid.add(opBtn("−"));
        grid.add(bigBtn("√", e -> unarySqrt()));
        // Row4
        grid.add(bigBtn("1", e -> inputDigit("1")));
        grid.add(bigBtn("2", e -> inputDigit("2")));
        grid.add(bigBtn("3", e -> inputDigit("3")));
        grid.add(opBtn("+"));
        grid.add(bigBtn("±", e -> toggleSign()));
        // Row5
        grid.add(bigBtn("MC", e -> mem = BigDecimal.ZERO));
        grid.add(bigBtn("MR", e -> setDisplay(mem)));
        grid.add(bigBtn("MS", e -> mem = getDisplayValue()));
        grid.add(bigBtn("M+", e -> mem = mem.add(getDisplayValue(), MC)));
        grid.add(bigBtn("M-", e -> mem = mem.subtract(getDisplayValue(), MC)));


        JPanel bottom = new JPanel(new GridLayout(1, 3, 10, 10));
        bottom.add(bigBtn("0", e -> inputDigit("0")));
        bottom.add(bigBtn(".", e -> inputDot()));
        bottom.add(equalBtn());

        JPanel south = new JPanel(new BorderLayout(8,8));
        south.add(bottom, BorderLayout.NORTH);
        south.add(angleBar(deg, rad, grad), BorderLayout.SOUTH);

        p.add(grid, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildScientific(JToggleButton deg, JToggleButton rad, JToggleButton grad) {
        String[] row1 = {"1/x","MC","MR","MS","M+","M-","C","Back","CE","%","÷"};
        String[] row2 = {"√","∛","x²","x³","xʸ","e","7","8","9","×"};
        String[] row3 = {"π","Exp","ln","log","n!","eˣ","4","5","6","−"};
        String[] row4 = {"(",")","cos","cosh","tan","tanh","1","2","3","+"};
        String[] row5 = {"Deg","Rad","Grad","Inv","sin","sinh","0",".","="};

        JPanel grid = new JPanel(new GridLayout(5, 0, 10, 10));
        grid.add(rowPanel(row1));
        grid.add(rowPanel(row2));
        grid.add(rowPanel(row3));
        grid.add(rowPanel(row4));
        grid.add(rowPanel(row5));

        // replace first three cells of last row with our shared toggles
        JPanel last = (JPanel) grid.getComponent(4);
        last.remove(0); last.add(deg, 0);
        last.remove(1); last.add(rad, 1);
        last.remove(2); last.add(grad, 2);

        JPanel wrap = new JPanel(new BorderLayout(10,10));
        wrap.add(grid, BorderLayout.CENTER);
        wrap.add(angleBar(deg, rad, grad), BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel rowPanel(String[] labels) {
        JPanel r = new JPanel(new GridLayout(1, labels.length, 10, 10));
        for (String s : labels) {
            switch (s) {
                case "÷","×","−","+","xʸ" -> r.add(opBtn(s));
                case "=" -> r.add(equalBtn());
                case "." -> r.add(bigBtn(".", e -> inputDot()));
                case "Back" -> r.add(bigBtn("Back", e -> backspace()));
                case "CE" -> r.add(bigBtn("CE", e -> clearEntry()));
                case "C" -> r.add(bigBtn("C", e -> clearAll()));
                case "%" -> r.add(bigBtn("%", e -> percent()));
                case "1/x" -> r.add(bigBtn("1/x", e -> unaryReciprocal()));
                case "√" -> r.add(bigBtn("√", e -> unarySqrt()));
                case "∛" -> r.add(bigBtn("∛", e -> setDisplay(BigDecimal.valueOf(Math.cbrt(getDisplayValue().doubleValue())))));
                case "x²" -> r.add(bigBtn("x²", e -> setDisplay(getDisplayValue().pow(2, MC))));
                case "x³" -> r.add(bigBtn("x³", e -> setDisplay(getDisplayValue().pow(3, MC))));
                case "e" -> r.add(bigBtn("e", e -> setDisplay(BigDecimal.valueOf(Math.E))));
                case "π" -> r.add(bigBtn("π", e -> setDisplay(BigDecimal.valueOf(Math.PI))));
                case "ln" -> r.add(bigBtn("ln", e -> setDisplay(BigDecimal.valueOf(Math.log(getDisplayValue().doubleValue())))));
                case "log" -> r.add(bigBtn("log", e -> setDisplay(BigDecimal.valueOf(Math.log10(getDisplayValue().doubleValue())))));
                case "eˣ" -> r.add(bigBtn("eˣ", e -> setDisplay(BigDecimal.valueOf(Math.exp(getDisplayValue().doubleValue())))));
                case "n!" -> r.add(bigBtn("n!", e -> setDisplay(factorial(getDisplayValue()))));
                case "sin" -> r.add(bigBtn("sin", e -> trig("sin", false)));
                case "cos" -> r.add(bigBtn("cos", e -> trig("cos", false)));
                case "tan" -> r.add(bigBtn("tan", e -> trig("tan", false)));
                case "sinh" -> r.add(bigBtn("sinh", e -> trig("sin", true)));
                case "cosh" -> r.add(bigBtn("cosh", e -> trig("cos", true)));
                case "tanh" -> r.add(bigBtn("tanh", e -> trig("tan", true)));
                case "Inv" -> r.add(bigBtn("Inv", e -> toggleInv())); // visual placeholder
                case "Deg","Rad","Grad" -> r.add(bigToggle(s, s.equals("Deg")));
                case "MC","MR","MS","M+","M-" -> r.add(bigBtn(s, e -> {
                    switch (s) {
                        case "MC" -> mem = BigDecimal.ZERO;
                        case "MR" -> setDisplay(mem);
                        case "MS" -> mem = getDisplayValue();
                        case "M+" -> mem = mem.add(getDisplayValue(), MC);
                        case "M-" -> mem = mem.subtract(getDisplayValue(), MC);
                    }
                }));
                case "(" , ")" -> r.add(bigBtn(s, e -> {})); // visual only
                case "Exp" -> r.add(bigBtn("Exp", e -> inputExp()));
                default -> {
                    if (s.matches("\\d")) r.add(bigBtn(s, e -> inputDigit(s)));
                    else throw new IllegalArgumentException("Unknown: " + s);
                }
            }
        }
        return r;
    }

    private JPanel angleBar(JToggleButton deg, JToggleButton rad, JToggleButton grad) {
        JPanel angle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        angle.add(deg); angle.add(rad); angle.add(grad);
        return angle;
    }

    // === Button factories with color styling ===
    private JButton bigBtn(String text, java.awt.event.ActionListener action) {
        JButton b = new JButton(text);
        b.addActionListener(action);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        b.setPreferredSize(new Dimension(90, 70));
        styleButton(b, text);
        return b;
    }

    private void styleButton(JButton b, String label) {
        // Colors similar to common calculator UIs
        Color digitBg = new Color(245, 245, 245);   // light gray
        Color funcBg  = new Color(0, 137, 123);     // teal for scientific/unary
        Color opBg    = new Color(46, 125, 50);     // green for + - × ÷ %
        Color eqBg    = new Color(21, 101, 192);    // blue =
        Color clearBg = new Color(198, 40, 40);     // red C/CE
        Color editBg  = new Color(211, 47, 47);     // red-ish Back
        Color memBg   = new Color(224, 224, 224);   // medium gray

        Color fgDark = new Color(33, 33, 33);
        Color fgLight = Color.WHITE;

        String t = label;
        boolean isDigit = t.matches("\\d") || t.equals(".") || t.equals("(") || t.equals(")");
        boolean isOp = "÷×−+xʸ%".contains(t);
        boolean isEq = t.equals("=");
        boolean isClear = t.equals("C") || t.equals("CE");
        boolean isEdit = t.equals("Back");
        boolean isMem = t.equals("MC") || t.equals("MR") || t.equals("MS") || t.equals("M+") || t.equals("M-");

        if (isDigit) { b.setBackground(digitBg); b.setForeground(fgDark); }
        else if (isOp) { b.setBackground(opBg); b.setForeground(fgLight); }
        else if (isEq) { b.setBackground(eqBg); b.setForeground(fgLight); }
        else if (isClear) { b.setBackground(clearBg); b.setForeground(fgLight); }
        else if (isEdit) { b.setBackground(editBg); b.setForeground(fgLight); }
        else if (isMem) { b.setBackground(memBg); b.setForeground(fgDark); }
        else { b.setBackground(funcBg); b.setForeground(fgLight); } // scientific/unary

        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200)),
                BorderFactory.createEmptyBorder(8,8,8,8)));
    }

    private JToggleButton bigToggle(String text, boolean selected) {
        JToggleButton b = new JToggleButton(text, selected);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        b.setPreferredSize(new Dimension(100, 44));
        return b;
    }

    private JButton opBtn(String op) {
        return bigBtn(op, e -> {
            applyPending();
            left = getDisplayValue();
            pendingOp = op;
            enteringNew = true;
        });
    }

    private JButton equalBtn() {
        return bigBtn("=", e -> {
            if (pendingOp != null) {
                BigDecimal right = getDisplayValue();
                setDisplay(compute(left, right, pendingOp));
                pendingOp = null;
                enteringNew = true;
            }
        });
    }

    // === Input helpers ===
    private void inputDigit(String d) {
        if (enteringNew || display.getText().equals("0")) {
            display.setText(d);
            enteringNew = false;
        } else {
            display.setText(display.getText() + d);
        }
    }
    private void inputDot() {
        if (enteringNew) {
            display.setText("0.");
            enteringNew = false;
        } else if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }
    private void toggleSign() {
        if (display.getText().equals("0")) return;
        if (display.getText().startsWith("-"))
            display.setText(display.getText().substring(1));
        else display.setText("-" + display.getText());
    }
    private void backspace() {
        if (enteringNew) return;
        String s = display.getText();
        if (s.length() <= 1 || (s.length()==2 && s.startsWith("-"))) {
            display.setText("0");
            enteringNew = true;
        } else display.setText(s.substring(0, s.length()-1));
    }
    private void clearEntry() { display.setText("0"); enteringNew = true; }
    private void clearAll() { clearEntry(); left = BigDecimal.ZERO; pendingOp = null; }

    private void percent() {
        if (pendingOp == null) return;
        BigDecimal a = left;
        BigDecimal b = getDisplayValue();
        BigDecimal p = a.multiply(b).divide(BigDecimal.valueOf(100), MC);
        switch (pendingOp) {
            case "+","−" -> setDisplay(a.add(p, MC));
            case "×","÷" -> setDisplay(p);
        }
        enteringNew = true;
    }

    private void unarySqrt() {
        double v = getDisplayValue().doubleValue();
        if (v < 0) { setDisplay(BigDecimal.ZERO); return; }
        setDisplay(BigDecimal.valueOf(Math.sqrt(v)));
    }
    private void unaryReciprocal() {
        BigDecimal v = getDisplayValue();
        if (v.compareTo(BigDecimal.ZERO) == 0) { setDisplay(BigDecimal.ZERO); return; }
        setDisplay(BigDecimal.ONE.divide(v, MC));
    }

    private void trig(String which, boolean hyper) {
        double x = getDisplayValue().doubleValue();
        if (!hyper) {
            double rad = x;
            if (getAngleMode().equals("Deg")) rad = Math.toRadians(x);
            else if (getAngleMode().equals("Grad")) rad = x * Math.PI / 200.0;
            switch (which) {
                case "sin" -> setDisplay(BigDecimal.valueOf(Math.sin(rad)));
                case "cos" -> setDisplay(BigDecimal.valueOf(Math.cos(rad)));
                case "tan" -> setDisplay(BigDecimal.valueOf(Math.tan(rad)));
            }
        } else {
            switch (which) {
                case "sin" -> setDisplay(BigDecimal.valueOf(Math.sinh(x)));
                case "cos" -> setDisplay(BigDecimal.valueOf(Math.cosh(x)));
                case "tan" -> setDisplay(BigDecimal.valueOf(Math.tanh(x)));
            }
        }
    }

    private String getAngleMode() {
        for (Component c : ((JPanel)((BorderLayout)getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.CENTER)).getComponents()) {
            if (c instanceof JTabbedPane tabs) {
                Component sc = tabs.getSelectedComponent();
                for (Component cc : ((JPanel) sc).getComponents()) {
                    if (cc instanceof JPanel inner) {
                        for (Component t : inner.getComponents()) {
                            if (t instanceof JPanel bar) {
                                for (Component k : bar.getComponents()) {
                                    if (k instanceof JToggleButton tb && tb.isSelected()) return tb.getText();
                                }
                            }
                        }
                    }
                }
            }
        }
        return "Deg";
    }

    private void toggleInv() { /* placeholder to mirror UI; not used */ }

    private static BigDecimal factorial(BigDecimal n) {
        int k = n.intValue();
        if (k < 0) return BigDecimal.ZERO;
        BigDecimal r = BigDecimal.ONE;
        for (int i=2; i<=k; i++) r = r.multiply(BigDecimal.valueOf(i), MC);
        return r;
    }

    private void applyPending() {
        if (pendingOp != null && !enteringNew) {
            BigDecimal right = getDisplayValue();
            setDisplay(compute(left, right, pendingOp));
        }
    }

    private BigDecimal compute(BigDecimal a, BigDecimal b, String op) {
        return switch (op) {
            case "+" -> a.add(b, MC);
            case "−" -> a.subtract(b, MC);
            case "×" -> a.multiply(b, MC);
            case "÷" -> b.compareTo(BigDecimal.ZERO)==0 ? BigDecimal.ZERO : a.divide(b, MC);
            case "xʸ" -> BigDecimal.valueOf(Math.pow(a.doubleValue(), b.doubleValue()));
            default -> b;
        };
    }

    private BigDecimal getDisplayValue() {
        return new BigDecimal(display.getText());
    }
    private void setDisplay(BigDecimal v) {
        String s = v.stripTrailingZeros().toPlainString();
        display.setText(s);
        enteringNew = true;
    }

    private void inputExp() {
        if (enteringNew) {
            display.setText("1E");
            enteringNew = false;
        } else if (!display.getText().contains("E")) {
            display.setText(display.getText() + "E");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            CalculatorApp app = new CalculatorApp();
            app.setVisible(true);
        });
    }
}
