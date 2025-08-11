import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Rectangle;

public class JobSequencing extends JFrame {

    private JTextField jobField, timeM1Field, timeM2Field;
    private JTextArea resultArea;
    private List<Job> jobs;
    private GanttChartPanel ganttChartPanel;
    private JScrollPane scrollPane;

    public JobSequencing() {
        setTitle("Job Sequencing with 2 Machines");
        setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Set full screen by default
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        jobs = new ArrayList<>();

        // Panel for input fields
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));

        inputPanel.add(new JLabel("Job Name:"));
        jobField = new JTextField();
        inputPanel.add(jobField);

        inputPanel.add(new JLabel("Time on Machine 1 (hours):"));
        timeM1Field = new JTextField();
        inputPanel.add(timeM1Field);

        inputPanel.add(new JLabel("Time on Machine 2 (hours):"));
        timeM2Field = new JTextField();
        inputPanel.add(timeM2Field);

        JButton addButton = new JButton("Add Job");
        inputPanel.add(addButton);

        JButton processButton = new JButton("Process Jobs");
        inputPanel.add(processButton);

        addButton.addActionListener(e -> addJob());
        processButton.addActionListener(e -> processJobs());

        // Panel for results and Gantt chart
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setPreferredSize(new Dimension(400, 200)); // Reduced size for the text area
        JScrollPane resultScrollPane = new JScrollPane(resultArea);

        // Gantt Chart Panel and Scroll Pane
        ganttChartPanel = new GanttChartPanel();
        scrollPane = new JScrollPane(ganttChartPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling

        add(inputPanel, BorderLayout.NORTH);
        add(resultScrollPane, BorderLayout.WEST); // Placed to the left
        add(scrollPane, BorderLayout.CENTER); // Gantt chart takes center space
    }

    private void addJob() {
        String jobName = jobField.getText();
        int timeM1 = Integer.parseInt(timeM1Field.getText());
        int timeM2 = Integer.parseInt(timeM2Field.getText());

        jobs.add(new Job(jobName, timeM1, timeM2));

        jobField.setText("");
        timeM1Field.setText("");
        timeM2Field.setText("");

        resultArea.append("Added Job: " + jobName + " (M1: " + timeM1 + " hours, M2: " + timeM2 + " hours)\n");
    }

    private void processJobs() {
        // Process jobs using Johnson's Algorithm
        Job[] jobSequence = johnsonAlgorithm(jobs.toArray(new Job[0]));

        resultArea.append("\nJob Sequencing Result:\n");
        for (Job job : jobSequence) {
            resultArea.append(job.name + " -> ");
        }
        resultArea.append("\n");

        // Show Gantt Chart
        ganttChartPanel.setJobSequence(jobSequence);
        ganttChartPanel.repaint();

        // Dynamically set the preferred size of the Gantt chart panel based on job count and durations
        ganttChartPanel.setPreferredSize(new Dimension(1500, 300)); // Made Gantt chart panel bigger
        scrollPane.revalidate(); // Make sure the scroll bars appear after resizing the panel

        // Calculate and display total elapsed time and idle time
        calculateAndDisplayTimes(jobSequence);
    }

    private Job[] johnsonAlgorithm(Job[] jobs) {
        int n = jobs.length;
        Job[] sequence = new Job[n];
        int left = 0, right = n - 1;

        Arrays.sort(jobs, (j1, j2) -> Integer.compare(Math.min(j1.timeM1, j1.timeM2), Math.min(j2.timeM1, j2.timeM2)));

        for (Job job : jobs) {
            if (job.timeM1 <= job.timeM2) {
                sequence[left++] = job;
            } else {
                sequence[right--] = job;
            }
        }

        return sequence;
    }

    private void calculateAndDisplayTimes(Job[] jobSequence) {
        int timeM1 = 0, timeM2 = 0;
        int idleTimeM1 = 0, idleTimeM2 = 0;

        for (Job job : jobSequence) {
            timeM1 += job.timeM1;

            if (timeM1 > timeM2) {
                idleTimeM2 += timeM1 - timeM2; // Machine 2 idle time
            }

            timeM2 = Math.max(timeM2, timeM1) + job.timeM2;
        }

        int totalElapsedTime = Math.max(timeM1, timeM2);

        resultArea.append("Total Elapsed Time: " + totalElapsedTime + " hours\n");
        resultArea.append("Idle Time on Machine 1: " + idleTimeM1 + " hours\n");
        resultArea.append("Idle Time on Machine 2: " + idleTimeM2 + " hours\n");
    }

    class GanttChartPanel extends JPanel {
        private Job[] jobSequence;
        private List<Rectangle> m1Rectangles;
        private List<Rectangle> m2Rectangles;
        private String tooltipText;
        private int totalHours; // To calculate the x-axis scale dynamically

        public GanttChartPanel() {
            m1Rectangles = new ArrayList<>();
            m2Rectangles = new ArrayList<>();
            tooltipText = "";

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int mouseX = e.getX();
                    int mouseY = e.getY();

                    for (int i = 0; i < m1Rectangles.size(); i++) {
                        if (m1Rectangles.get(i).contains(mouseX, mouseY)) {
                            Job job = jobSequence[i];
                            tooltipText = "Job: " + job.name + ", M1 Duration: " + job.timeM1 + " hours";
                            repaint();
                            return;
                        } else if (m2Rectangles.get(i).contains(mouseX, mouseY)) {
                            Job job = jobSequence[i];
                            tooltipText = "Job: " + job.name + ", M2 Duration: " + job.timeM2 + " hours";
                            repaint();
                            return;
                        }
                    }

                    tooltipText = "";
                    repaint();
                }
            });
        }

        public void setJobSequence(Job[] jobSequence) {
            this.jobSequence = jobSequence;
            totalHours = calculateTotalHours(jobSequence); // Calculate total hours for dynamic x-axis
        }

        private int calculateTotalHours(Job[] jobSequence) {
            int maxTimeM1 = 0, maxTimeM2 = 0;

            for (Job job : jobSequence) {
                maxTimeM1 += job.timeM1;
                maxTimeM2 = Math.max(maxTimeM2, maxTimeM1) + job.timeM2;
            }

            return Math.max(maxTimeM1, maxTimeM2);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (jobSequence == null) return;

            m1Rectangles.clear();
            m2Rectangles.clear();

            int x = 50;
            int y1 = 50, y2 = 100; // Positions for M1 and M2
            int hourWidth = 30;
            int rowHeight = 40;

            // Draw axes and labels
            g.setColor(Color.BLACK);
            g.drawLine(50, 30, getWidth() - 50, 30); // X-axis
            g.drawLine(50, 30, 50, getHeight() - 50); // Y-axis

            // Draw hours on the X-axis dynamically based on totalHours
            for (int i = 0; i <= totalHours; i++) {
                g.drawString(i + "h", 50 + i * hourWidth, 25);
            }

            // Label the Y-axis as M1 and M2 for the two machines
            g.drawString("M1", 10, y1 + 20);
            g.drawString("M2", 10, y2 + 20);

            int m1X = x; // X-coordinate for M1
            int m2X = x; // X-coordinate for M2

            int m1EndTime = 0, m2EndTime = 0; // End times for each machine

            // Set font for job names
            g.setFont(new Font("Arial", Font.BOLD, 16)); // Change to a larger font size

            for (int i = 0; i < jobSequence.length; i++) {
                Job job = jobSequence[i];

                // Machine 1 job bar
                g.setColor(Color.BLUE);
                g.fillRect(m1X, y1, job.timeM1 * hourWidth, rowHeight);
                g.setColor(Color.WHITE); // Set text color to white
                g.drawString(job.name, m1X + 5, y1 + 20);
                g.setColor(Color.BLACK);
                g.drawRect(m1X, y1, job.timeM1 * hourWidth, rowHeight);
                m1Rectangles.add(new Rectangle(m1X, y1, job.timeM1 * hourWidth, rowHeight));
                m1EndTime = m1X + job.timeM1 * hourWidth;
                m1X += job.timeM1 * hourWidth;

                // Machine 2 job bar
                int m2StartTime = Math.max(m2X, m1EndTime); // Machine 2 starts after M1 is done or continues
                int idleTimeM2 = m2StartTime - m2X;

                if (idleTimeM2 > 0) {
                    // Draw green idle time bar
                    g.setColor(Color.GREEN);
                    g.fillRect(m2X, y2, idleTimeM2, rowHeight);
                    g.setColor(Color.BLACK);
                    g.drawString("Idle", m2X + 5, y2 + 20);
                    g.drawRect(m2X, y2, idleTimeM2, rowHeight);
                }

                g.setColor(Color.RED);
                g.fillRect(m2StartTime, y2, job.timeM2 * hourWidth, rowHeight);
                g.setColor(Color.WHITE); // Set text color to white for M2 job names
                g.drawString(job.name, m2StartTime + 5, y2 + 20);
                g.setColor(Color.BLACK);
                g.drawRect(m2StartTime, y2, job.timeM2 * hourWidth, rowHeight);
                m2Rectangles.add(new Rectangle(m2StartTime, y2, job.timeM2 * hourWidth, rowHeight));

                // Draw arrow between M1 and M2
                drawArrow(g, m1EndTime, y1 + rowHeight / 2, m2StartTime, y2 + rowHeight / 2);

                m2EndTime = m2StartTime + job.timeM2 * hourWidth;
                m2X = m2EndTime;
            }

            // Display tooltip
            if (!tooltipText.isEmpty()) {
                g.setColor(Color.BLACK);
                g.drawString(tooltipText, 100, 150);
            }
        }

        private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
            g.drawLine(x1, y1, x2, y2);
            int arrowSize = 10;
            double angle = Math.atan2(y2 - y1, x2 - x1);
            g.fillPolygon(new int[]{
                x2, x2 - (int) (arrowSize * Math.cos(angle - Math.PI / 6)), x2 - (int) (arrowSize * Math.cos(angle + Math.PI / 6))
            }, new int[]{
                y2, y2 - (int) (arrowSize * Math.sin(angle - Math.PI / 6)), y2 - (int) (arrowSize * Math.sin(angle + Math.PI / 6))
            }, 3);
        }
    }

    class Job {
        String name;
        int timeM1;
        int timeM2;

        public Job(String name, int timeM1, int timeM2) {
            this.name = name;
            this.timeM1 = timeM1;
            this.timeM2 = timeM2;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JobSequencing jobSequencing = new JobSequencing();
            jobSequencing.setVisible(true);
        });
    }
}