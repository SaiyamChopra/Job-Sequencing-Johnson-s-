Job Sequencing with 2 Machines (Johnson's Algorithm)

📌 Overview
This is a Java Swing application that schedules jobs on two machines using Johnson’s Algorithm for minimizing the total completion time (makespan).
It provides:
An interactive GUI to add jobs with their processing times.
Automatic optimal job sequencing.
A dynamic Gantt chart visualization with idle times highlighted.
Calculation of total elapsed time and idle times for both machines.

✨ Features
Add Jobs Dynamically — Specify job name, Machine 1 time, and Machine 2 time.
Johnson’s Algorithm Implementation — Finds the optimal order of jobs to minimize makespan.
Interactive Gantt Chart:
Shows job execution timeline for Machine 1 (blue) and Machine 2 (red).
Displays idle times in green.
Hover over bars to see job details.
Time Analysis — Displays total elapsed time, idle time for each machine.

🧮 How It Works
User Input:
Job name (e.g., J1)
Processing time on Machine 1 (hours)
Processing time on Machine 2 (hours)
Johnson’s Algorithm Steps:
Sort jobs based on the smaller processing time (either M1 or M2).
Assign jobs with smaller M1 times to the front of the sequence.
Assign jobs with smaller M2 times to the end of the sequence.
Visualization:
Jobs on Machine 1 start immediately if available.
Jobs on Machine 2 start only when both:
Machine 1 has completed that job.
Machine 2 is free.
Idle times are marked in green.

🖥️ GUI Layout
Top Panel: Input fields for job details and buttons to Add or Process jobs.
Left Panel: Text area showing job list, sequencing result, and times.
Right Panel: Scrollable Gantt chart visualization.

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/e5538d48-afb0-4581-9085-0974d0ea57db" />
