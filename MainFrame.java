
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import com.jaunt.*;


public class MainFrame extends JFrame
{
	private final int MAX_NUM_STOCKS = 50;
	
	private String[] symbols;
	private String[] names;
	private double[] shares;
	private double[] prices;
	private double[] mktvalues;
	private double[] changeDollar;
	private double[] changePercent;
	private double[] gains;
	private int numStocks;
		
	private JButton buttonImport;
	private JButton buttonUpdate;
	private JButton buttonAbout;
	private JLabel labelValue;
	private JLabel labelNumber;
	private JLabel labelChangeDollar;
	private JLabel labelChangePercent;
	private JTable table;
	private DefaultTableModel tmodel;
	
	public MainFrame()
	{
		symbols = new String[MAX_NUM_STOCKS];
		names = new String[MAX_NUM_STOCKS];
		shares = new double[MAX_NUM_STOCKS];
		prices = new double[MAX_NUM_STOCKS];
		mktvalues = new double[MAX_NUM_STOCKS];
		changeDollar = new double[MAX_NUM_STOCKS];
		changePercent = new double[MAX_NUM_STOCKS];
		gains = new double[MAX_NUM_STOCKS];
		numStocks = 0;
		
		
		createGUI();

		buttonImport.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					try
					{
						readData(selectFile());
						updateTable();
					}
					catch(NullPointerException e)
					{
						JOptionPane.showMessageDialog(null,"No File Selected!");
					}
					
					
					catch(NoSuchElementException e)
					{
						JOptionPane.showMessageDialog(null,"Incorrect File Format!");
						updateTable();
					}
					catch(IndexOutOfBoundsException e)
					{
						JOptionPane.showMessageDialog(null,"Exceeding the Maximum Number of Stocks (" + MAX_NUM_STOCKS + ")!");
						updateTable();
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(null,"Importing Portfolio Error!");
						updateTable();
					}
				}
			
			});
		
		buttonUpdate.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					if (numStocks == 0)
					{
						JOptionPane.showMessageDialog(null,"Please Import a Portfolio First!");
						return;
					}
					try
					{
						getQuotes();
						updateValues();
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(null,"Failed to Get Quotes!\n"+e.toString());
					}
				}
			
			});
	}
	
	
		
	public void createGUI()
	{
		String[] columnNames = {"Symbol", "Name", "Quantity", "Price", "Mkt Value", "$ Change", "% Change", "Today's G/L"};
		tmodel = new DefaultTableModel(null, columnNames)
		  {
		    public boolean isCellEditable(int row, int column)
		    {
		      return false;
		    }
		  };
		table = new JTable(tmodel);
		table.setFillsViewportHeight(true);
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
		
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		for (int i=2; i<columnNames.length; i++)
		{
			table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
		}
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(180);
		
		JLabel label1 = new JLabel("Total Market Value: ");
		JLabel label2 = new JLabel("Number of Stocks: ");
		JLabel label3 = new JLabel("Today's Gain/Loss ($): ");
		JLabel label4 = new JLabel("Today's Gain/Loss (%): ");
		
		labelValue = new JLabel("");
		labelNumber = new JLabel("");
		labelChangePercent = new JLabel("");
		labelChangeDollar = new JLabel("");
		
		JPanel panelLabels = new JPanel(new GridLayout(2,4));
		panelLabels.add(label1);
		panelLabels.add(labelValue);
		panelLabels.add(label3);
		panelLabels.add(labelChangeDollar);
		panelLabels.add(label2);
		panelLabels.add(labelNumber);
		panelLabels.add(label4);
		panelLabels.add(labelChangePercent);

		JPanel panelTemp1 = new JPanel();
		panelTemp1.setPreferredSize(new Dimension(100, 40));
		JPanel panelTemp2 = new JPanel();
		
		JPanel panelLabelsSurround = new JPanel(new BorderLayout());
		panelLabelsSurround.add(panelTemp1, BorderLayout.WEST);
		panelLabelsSurround.add(panelLabels, BorderLayout.CENTER);
		panelLabelsSurround.add(panelTemp2, BorderLayout.EAST);
			
		buttonImport = new JButton("Import Portfolio");
		buttonImport.setPreferredSize(new Dimension(140, 25));
		buttonUpdate = new JButton("Update");
		buttonUpdate.setPreferredSize(new Dimension(140, 25));	
		buttonAbout = new JButton("About");
		buttonAbout.setPreferredSize(new Dimension(140, 25));
		
		
		JPanel panelButtons = new JPanel();
		panelButtons.add(buttonImport);
		panelButtons.add(buttonUpdate);
		panelButtons.add(buttonAbout);
		buttonAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null, "\u00a9 2018 - MGT3745 - GATECH \n David Ding (dding44) and Brad Morgan (jmorgan67)");
			}
		
			
			
		});
		
		
		JPanel panelBottom = new JPanel(new GridLayout(2,1,5,20));
		panelBottom.add(panelLabelsSurround);
		panelBottom.add(panelButtons);
		
		add(panelBottom, BorderLayout.SOUTH);
				
		setSize(800, 300);
		setTitle("Portfolio Monitor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);		
	}
	
	public File selectFile()
	{
		JFileChooser fileChooser = new JFileChooser(".");
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.CANCEL_OPTION)
		{
			throw new NullPointerException();
		}
			
		return fileChooser.getSelectedFile();
	}
	
	public void readData(File infile) throws Exception
	{
		Scanner input = new Scanner(infile);
		numStocks = 0;
		while (input.hasNext()) {
			symbols[numStocks] = input.next();
			shares[numStocks] = input.nextDouble();
			numStocks += 1;
		}
		
		
	}
	
	public void updateTable()
	{
		tmodel.setRowCount(numStocks);
		for (int i=0; i < numStocks; i++)
		{
			table.setValueAt(symbols[i], i, 0);
			table.setValueAt("", i, 1);
			table.setValueAt(String.format("%,.2f", shares[i]), i, 2);
			table.setValueAt("", i, 3);
			table.setValueAt("", i, 4);
			table.setValueAt("", i, 5);
			table.setValueAt("", i, 6);
			table.setValueAt("", i, 7);
		}
		labelValue.setText("");
		labelNumber.setText("");
		labelChangeDollar.setText("");
		labelChangePercent.setText("");
	}
	
	public void getQuotes() throws JauntException
	{	
		Element element;
		int end = 0;
		String url = "https://money.cnn.com/quote/quote.html?symb=";
		for (int i = 0; i < numStocks;i++) {
			UserAgent agent = new UserAgent();
			agent.visit(url + symbols[i]);
			element = agent.doc.findFirst("<div id=wsod_companyName>");
			String name = element.getTextContent().trim();
			end = name.indexOf('(');
			name = name.substring(0,end).trim();
			names[i] = name;
			element = agent.doc.findFirst("<td class=wsod_last>");
			String pricestr = element.getElement(0).getTextContent().trim();
			pricestr = pricestr.replaceAll(",", "");
			double price = Double.parseDouble(pricestr);
			prices[i] = price;
			element = agent.doc.findFirst("<td class=wsod_change>");
			String dollarChange = element.getElement(1).getTextContent().trim();
			double changeDollars = Double.parseDouble(dollarChange);
			changeDollar[i] = changeDollars;
			String changePCT = element.getElement(3).getTextContent().trim();
			end = changePCT.indexOf('%');
			changePCT = changePCT.substring(0, end);
			double changePercents = Double.parseDouble(changePCT);
			changePercent[i] = changePercents;
			
			
			
			
		}
		
		
		
	}
	
	public void updateValues()
	{
		double marketValue = 0;
		double totalGain = 0;
		double totalPCT = 0;
		for (int i = 0; i < numStocks; i++){
			mktvalues[i] = shares[i] * prices[i];
			gains[i] = changeDollar[i] * shares[i];
			table.setValueAt(names[i],i,1);
			table.setValueAt(String.format("%,.2f", prices[i]), i, 3);
			table.setValueAt(String.format("%,.2f", mktvalues[i]), i, 4);
			String str = String.format("%,.2f", changeDollar[i]);
			if (changeDollar[i] > 0) str = "+" + str;
			table.setValueAt(str, i, 5);
			str = String.format("%,.2f%%", changePercent[i]);
			if (changePercent[i] > 0) str = "+" + str;
			table.setValueAt(str, i, 6);
			str = String.format("%,.2f", gains[i]);
			if (gains[i] > 0) str = "+" + str;
			table.setValueAt(str, i, 7);
			
			
		}
		for (int i = 0; i < numStocks; i++) {
			marketValue += mktvalues[i];
			totalGain += gains[i];
			
			}
		totalPCT = totalGain/(marketValue - totalGain);
		labelValue.setText(String.format("%,.2f", marketValue));
		labelNumber.setText(String.format("%d",numStocks));
		labelChangeDollar.setText(String.format("%,.2f", totalGain));
		labelChangePercent.setText(String.format("%,.2f%%", totalPCT*100));
		
		
		
		
	}

}
