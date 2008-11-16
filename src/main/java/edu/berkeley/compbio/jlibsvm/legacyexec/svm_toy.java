package edu.berkeley.compbio.jlibsvm.legacyexec;


import edu.berkeley.compbio.jlibsvm.ContinuousModel;
import edu.berkeley.compbio.jlibsvm.DiscreteModel;
import edu.berkeley.compbio.jlibsvm.SVM;
import edu.berkeley.compbio.jlibsvm.SolutionModel;
import edu.berkeley.compbio.jlibsvm.SvmException;
import edu.berkeley.compbio.jlibsvm.SvmParameter;
import edu.berkeley.compbio.jlibsvm.SvmPoint;
import edu.berkeley.compbio.jlibsvm.SvmProblem;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationProblem;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationSVM;
import edu.berkeley.compbio.jlibsvm.binary.C_SVC;
import edu.berkeley.compbio.jlibsvm.binary.Nu_SVC;
import edu.berkeley.compbio.jlibsvm.kernel.GammaKernel;
import edu.berkeley.compbio.jlibsvm.kernel.KernelFunction;
import edu.berkeley.compbio.jlibsvm.kernel.LinearKernel;
import edu.berkeley.compbio.jlibsvm.kernel.PolynomialKernel;
import edu.berkeley.compbio.jlibsvm.kernel.PrecomputedKernel;
import edu.berkeley.compbio.jlibsvm.kernel.RBFKernel;
import edu.berkeley.compbio.jlibsvm.kernel.SigmoidKernel;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassProblem;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassificationSVM;
import edu.berkeley.compbio.jlibsvm.oneclass.OneClassSVC;
import edu.berkeley.compbio.jlibsvm.regression.EpsilonSVR;
import edu.berkeley.compbio.jlibsvm.regression.Nu_SVR;
import edu.berkeley.compbio.jlibsvm.regression.RegressionProblem;
import edu.berkeley.compbio.jlibsvm.regression.RegressionSVM;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;


public class svm_toy extends Applet
	{

	static final String DEFAULT_PARAM = "-t 2 -c 100";
	int XLEN;
	int YLEN;

	// off-screen buffer

	Image buffer;
	Graphics buffer_gc;

	// pre-allocated colors

	final static Color colors[] = {
			new Color(0, 0, 0),
			new Color(0, 120, 120),
			new Color(120, 120, 0),
			new Color(120, 0, 120),
			new Color(0, 200, 200),
			new Color(200, 200, 0),
			new Color(200, 0, 200)
	};

	class point
		{
		point(float x, float y, byte value)
			{
			this.x = x;
			this.y = y;
			this.value = value;
			}

		float x, y;
		byte value;
		}

	Vector<point> point_list = new Vector<point>();
	byte current_value = 1;

	public void init()
		{
		setSize(getSize());

		final Button button_change = new Button("Change");
		Button button_run = new Button("Run");
		Button button_clear = new Button("Clear");
		Button button_save = new Button("Save");
		Button button_load = new Button("Load");
		final TextField input_line = new TextField(DEFAULT_PARAM);

		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);

		Panel p = new Panel();
		GridBagLayout gridbag = new GridBagLayout();
		p.setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 1;
		gridbag.setConstraints(button_change, c);
		gridbag.setConstraints(button_run, c);
		gridbag.setConstraints(button_clear, c);
		gridbag.setConstraints(button_save, c);
		gridbag.setConstraints(button_load, c);
		c.weightx = 5;
		c.gridwidth = 5;
		gridbag.setConstraints(input_line, c);

		button_change.setBackground(colors[current_value]);

		p.add(button_change);
		p.add(button_run);
		p.add(button_clear);
		p.add(button_save);
		p.add(button_load);
		p.add(input_line);
		this.add(p, BorderLayout.SOUTH);

		button_change.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent e)
			{
			button_change_clicked();
			button_change.setBackground(colors[current_value]);
			}
		});

		button_run.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent e)
			{
			button_run_clicked(input_line.getText());
			}
		});

		button_clear.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent e)
			{
			button_clear_clicked();
			}
		});

		button_save.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent e)
			{
			button_save_clicked();
			}
		});

		button_load.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent e)
			{
			button_load_clicked();
			}
		});

		input_line.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent e)
			{
			button_run_clicked(input_line.getText());
			}
		});

		this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		}

	void draw_point(point p)
		{
		Color c = colors[p.value + 3];

		Graphics window_gc = getGraphics();
		buffer_gc.setColor(c);
		buffer_gc.fillRect((int) (p.x * XLEN), (int) (p.y * YLEN), 4, 4);
		window_gc.setColor(c);
		window_gc.fillRect((int) (p.x * XLEN), (int) (p.y * YLEN), 4, 4);
		}

	void clear_all()
		{
		point_list.removeAllElements();
		if (buffer != null)
			{
			buffer_gc.setColor(colors[0]);
			buffer_gc.fillRect(0, 0, XLEN, YLEN);
			}
		repaint();
		}

	void draw_all_points()
		{
		int n = point_list.size();
		for (int i = 0; i < n; i++)
			{
			draw_point(point_list.elementAt(i));
			}
		}

	void button_change_clicked()
		{
		++current_value;
		if (current_value > 3)
			{
			current_value = 1;
			}
		}


	void button_run_clicked(String args)
		{
		// guard
		if (point_list.isEmpty())
			{
			return;
			}

		SvmParameter param = new SvmParameter();

		// default values
		/*	param.svm_type = svm_parameter.C_SVC;
				param.kernel_type = svm_parameter.RBF;
				param.degree = 3;
				param.gamma = 0;
				param.coef0 = 0;*/
		param.nu = 0.5f;
		param.cache_size = 40;
		param.C = 1;
		param.eps = 1e-3f;
		param.p = 0.1f;
		param.shrinking = true;
		param.probability = false;
		//param.nr_weight = 0;
		//param.weightLabel = new int[0];
		//param.weight = new float[0];

		// parse options
		StringTokenizer st = new StringTokenizer(args);
		String[] argv = new String[st.countTokens()];
		for (int i = 0; i < argv.length; i++)
			{
			argv[i] = st.nextToken();
			}


		int svm_type = 0;
		int kernel_type = 2;
		int degree = 3;
		float gamma = 0;
		float coef0 = 0;

		for (int i = 0; i < argv.length; i++)
			{
			if (argv[i].charAt(0) != '-')
				{
				break;
				}
			if (++i >= argv.length)
				{
				System.err.print("unknown option\n");
				break;
				}
			switch (argv[i - 1].charAt(1))
				{
				case 's':
					svm_type = Integer.parseInt(argv[i]);
					break;
				case 't':
					kernel_type = Integer.parseInt(argv[i]);
					break;
				case 'd':
					degree = Integer.parseInt(argv[i]);
					break;
				case 'g':
					gamma = Float.parseFloat(argv[i]);
					break;
				case 'r':
					coef0 = Float.parseFloat(argv[i]);
					break;
				case 'n':
					param.nu = Float.parseFloat(argv[i]);
					break;
				case 'm':
					param.cache_size = Float.parseFloat(argv[i]);
					break;
				case 'c':
					param.C = Float.parseFloat(argv[i]);
					break;
				case 'e':
					param.eps = Float.parseFloat(argv[i]);
					break;
				case 'p':
					param.p = Float.parseFloat(argv[i]);
					break;
				case 'h':
					param.shrinking = Boolean.parseBoolean(argv[i]);
					break;
				case 'b':
					param.probability = Boolean.parseBoolean(argv[i]);
					break;
				case 'w':
					param.putWeight(Integer.parseInt(argv[i - 1].substring(2)), Float.parseFloat(argv[i]));
					break;
				default:
					System.err.print("unknown option\n");
				}
			}

		KernelFunction kernel;
		switch (kernel_type)
			{
			case svm_train.LINEAR:
				kernel = new LinearKernel();
				break;
			case svm_train.POLY:
				kernel = new PolynomialKernel(degree, gamma, coef0);
				break;
			case svm_train.RBF:
				kernel = new RBFKernel(gamma);
				break;
			case svm_train.SIGMOID:
				kernel = new SigmoidKernel(gamma, coef0);
				break;
			case svm_train.PRECOMPUTED:
				kernel = new PrecomputedKernel();
				break;
			default:
				throw new SvmException("Unknown kernel type: " + kernel_type);
			}

		SVM svm;
		switch (svm_type)
			{
			case svm_train.C_SVC:
				svm = new C_SVC(kernel, param);
				break;
			case svm_train.NU_SVC:
				svm = new Nu_SVC(kernel, param);
				break;
			case svm_train.ONE_CLASS:
				svm = new OneClassSVC(kernel, param);
				break;
			case svm_train.EPSILON_SVR:
				svm = new EpsilonSVR(kernel, param);
				break;
			case svm_train.NU_SVR:
				svm = new Nu_SVR(kernel, param);
				break;
			default:
				throw new SvmException("Unknown svm type: " + kernel_type);
			}

		Set<Byte> uniqueClasses = new HashSet<Byte>();
		for (point point : point_list)
			{
			uniqueClasses.add(point.value);
			}
		int numClasses = uniqueClasses.size();

		// build problem
		SvmProblem prob;
		if (svm instanceof RegressionSVM)
			{
			prob = new RegressionProblem(point_list.size());
			}
		else if (numClasses == 1)
			{
			prob = new RegressionProblem(point_list.size());
			}
		else if (numClasses == 2)
				{
				prob = new BinaryClassificationProblem(point_list.size());
				}
			else
				{
				prob = new MultiClassProblem<Byte>(Byte.class, point_list.size());
				}

		//prob.l = point_list.size();
		//prob.targetValues = new Float[point_list.size()];

		if (kernel_type == svm_train.PRECOMPUTED)
			{
			throw new SvmException("Can't use precomputed kernel with svm_toy");
			}
		else if (svm_type == svm_train.EPSILON_SVR || svm_type == svm_train.NU_SVR)
			{

			if (kernel instanceof GammaKernel && ((GammaKernel) kernel).getGamma() == 0f)
				{
				((GammaKernel) kernel).setGamma(1.0f);
				//gamma = 1;
				}

			//prob.examples = new SvmPoint[point_list.size()];
			for (int i = 0; i < point_list.size(); i++)
				{
				point p = point_list.elementAt(i);
				prob.examples[i] = new SvmPoint(1);
				prob.examples[i].indexes[0] = 1;
				prob.examples[i].values[0] = p.x;
				prob.putTargetValue(i, p.y);
				}

			// build model & classify
			ContinuousModel model = (ContinuousModel) svm.train(prob);
			SvmPoint x = new SvmPoint(1);
			//x[0] = new svm_node();
			x.indexes[0] = 1;
			int[] j = new int[XLEN];

			Graphics window_gc = getGraphics();
			for (int i = 0; i < XLEN; i++)
				{
				x.values[0] = (float) i / XLEN;
				j[i] = (int) (YLEN * model.predictValue(x));
				}

			buffer_gc.setColor(colors[0]);
			buffer_gc.drawLine(0, 0, 0, YLEN - 1);
			window_gc.setColor(colors[0]);
			window_gc.drawLine(0, 0, 0, YLEN - 1);

			int p = (int) (param.p * YLEN);
			for (int i = 1; i < XLEN; i++)
				{
				buffer_gc.setColor(colors[0]);
				buffer_gc.drawLine(i, 0, i, YLEN - 1);
				window_gc.setColor(colors[0]);
				window_gc.drawLine(i, 0, i, YLEN - 1);

				buffer_gc.setColor(colors[5]);
				window_gc.setColor(colors[5]);
				buffer_gc.drawLine(i - 1, j[i - 1], i, j[i]);
				window_gc.drawLine(i - 1, j[i - 1], i, j[i]);

				if (svm_type == svm_train.EPSILON_SVR)
					{
					buffer_gc.setColor(colors[2]);
					window_gc.setColor(colors[2]);
					buffer_gc.drawLine(i - 1, j[i - 1] + p, i, j[i] + p);
					window_gc.drawLine(i - 1, j[i - 1] + p, i, j[i] + p);

					buffer_gc.setColor(colors[2]);
					window_gc.setColor(colors[2]);
					buffer_gc.drawLine(i - 1, j[i - 1] - p, i, j[i] - p);
					window_gc.drawLine(i - 1, j[i - 1] - p, i, j[i] - p);
					}
				}
			}
		else
			{
			if (kernel instanceof GammaKernel && ((GammaKernel) kernel).getGamma() == 0f)
				{
				((GammaKernel) kernel).setGamma(0.5f);
				//gamma = 0.5f;
				}

			prob.examples = new SvmPoint[point_list.size()]; //[2];
			for (int i = 0; i < point_list.size(); i++)
				{
				point p = point_list.elementAt(i);
				prob.examples[i] = new SvmPoint(2);
				prob.examples[i].indexes[0] = 1;
				prob.examples[i].values[0] = p.x;
				prob.examples[i].indexes[1] = 2;
				prob.examples[i].values[1] = p.y;

				prob.putTargetValue(i, p.value);
				}

			if (svm instanceof BinaryClassificationSVM && prob.getNumLabels() > 2)
				{
				svm = new MultiClassificationSVM((BinaryClassificationSVM) svm);
				}
			// build model & classify
			SolutionModel model = svm.train(prob);
			SvmPoint x = new SvmPoint(2);
			//x[0] = new svm_node();
			//x[1] = new svm_node();
			x.indexes[0] = 1;
			x.indexes[1] = 2;

			Graphics window_gc = getGraphics();
			for (int i = 0; i < XLEN; i++)
				{
				for (int j = 0; j < YLEN; j++)
					{
					x.values[0] = (float) i / XLEN;
					x.values[1] = (float) j / YLEN;

					int d;
					if (model instanceof DiscreteModel)
						{
						// broken generics
						Object o = ((DiscreteModel) model).predictLabel(x);
						if (o instanceof Boolean)
							{
							d = ((Boolean) o) ? 1 : 2;
							}
						else if (o instanceof Integer)
							{
							d = (Integer) o;
							}
						else if (o instanceof Byte)
								{
								d = (Byte) o;
								}
						else
							{
							throw new SvmException("Don't know how to plot label of type " + o.getClass());
							}
						}
					else
						{
						d = ((ContinuousModel) model).predictValue(x).intValue();
						}

					/*if (svm_type == svm_train.ONE_CLASS && d < 0)
						{
						d = 2;
						}*/
					buffer_gc.setColor(colors[(int) d]);
					window_gc.setColor(colors[(int) d]);
					buffer_gc.drawLine(i, j, i, j);
					window_gc.drawLine(i, j, i, j);
					}
				}
			}

		draw_all_points();
		}

	void button_clear_clicked()
		{
		clear_all();
		}

	void button_save_clicked()
		{
		FileDialog dialog = new FileDialog(new Frame(), "Save", FileDialog.SAVE);
		dialog.setVisible(true);
		String filename = dialog.getDirectory() + File.separator + dialog.getFile();
		if (filename == null)
			{
			return;
			}
		try
			{
			DataOutputStream fp = new DataOutputStream(new FileOutputStream(filename));
			int n = point_list.size();
			for (int i = 0; i < n; i++)
				{
				point p = point_list.elementAt(i);
				fp.writeBytes(p.value + " 1:" + p.x + " 2:" + p.y + "\n");
				}
			fp.close();
			}
		catch (IOException e)
			{
			System.err.print(e);
			}
		}

	void button_load_clicked()
		{
		FileDialog dialog = new FileDialog(new Frame(), "Load", FileDialog.LOAD);
		dialog.setVisible(true);
		//String filename = dialog.getFile();
		String filename = dialog.getDirectory() + File.separator + dialog.getFile();
		if (filename == null)
			{
			return;
			}
		clear_all();
		try
			{
			BufferedReader fp = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = fp.readLine()) != null)
				{
				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
				byte value = (byte) Integer.parseInt(st.nextToken());
				st.nextToken();
				float x = Float.parseFloat(st.nextToken());
				st.nextToken();
				float y = Float.parseFloat(st.nextToken());
				point_list.addElement(new point(x, y, value));
				}
			fp.close();
			}
		catch (IOException e)
			{
			System.err.print(e);
			}
		draw_all_points();
		}

	protected void processMouseEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_PRESSED)
			{
			if (e.getX() >= XLEN || e.getY() >= YLEN)
				{
				return;
				}
			point p = new point((float) e.getX() / XLEN, (float) e.getY() / YLEN, current_value);
			point_list.addElement(p);
			draw_point(p);
			}
		}

	public void paint(Graphics g)
		{
		// create buffer first time
		if (buffer == null)
			{
			buffer = this.createImage(XLEN, YLEN);
			buffer_gc = buffer.getGraphics();
			buffer_gc.setColor(colors[0]);
			buffer_gc.fillRect(0, 0, XLEN, YLEN);
			}
		g.drawImage(buffer, 0, 0, this);
		}

	public Dimension getPreferredSize()
		{
		return new Dimension(XLEN, YLEN + 50);
		}

	public void setSize(Dimension d)
		{
		setSize(d.width, d.height);
		}

	public void setSize(int w, int h)
		{
		super.setSize(w, h);
		XLEN = w;
		YLEN = h - 50;
		clear_all();
		}

	public static void main(String[] argv)
		{
		new AppletFrame("svm_toy", new svm_toy(), 500, 500 + 50);
		}
	}