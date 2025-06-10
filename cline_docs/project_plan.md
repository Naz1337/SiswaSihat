Of course! This is an excellent and detailed assignment. Let's break it down and build a solid, achievable plan for your group of four.

Based on your requirements (Android Studio with Java/XML, 10 activities, 4 sensors, Firestore for database), here is a complete brainstorming and project plan for your group.

### **1. App Concept & Focus Area**

First, let's choose a name and a focus area that is specific but broad enough to accommodate all the required features.

*   **App Name:** **SiswaSihat** (A blend of Malay "Siswa" for Student and "Sihat" for Healthy) or **CampusCalm**. Let's go with **CampusCalm** for this plan as it's clear and professional.
*   **Chosen Focus Area:** **Holistic Stress Management & Wellness Routine Building**.
*   **Project Aim:** To develop **CampusCalm**, a mobile application that provides Malaysian university students with a suite of tools for managing academic stress, building healthy self-care routines, and accessing immediate support, thereby promoting a balanced and mentally healthy university life.

---

### **2. App Structure: 10 Activities Breakdown**

Here is a plan for the 10 required activities, distributing the work, including the 4 sensors, among your 4 group members.

*   **Core Pages (to be built first, perhaps by one member or split):**
    1.  **Login Activity:** Simple UI with `EditText` for username and password, and a `Button` to log in. It will query the Firestore "users" collection to check credentials.
    2.  **Register Activity:** `EditText` for username and password, and a `Button` to register. This will create a new document in the Firestore "users" collection with the plaintext username and password.

*   **Main Functional Pages (8 pages, 2 per member):**
    The main screen after login will be a **Dashboard** that uses a `GridView` or `RecyclerView` to navigate to all other features. This will be the first functional page for Member 1.

---

### **3. Detailed Task Distribution for 4 Members**

Here’s a clear breakdown of who does what.

#### **Member 1: Planning & Core Navigation**

This member focuses on the app's structure and helping the user organize their life.

*   **Activity 1: Dashboard (Home Screen)**
    *   **Function:** This is the main hub. It will use a **`GridView`** to display large, clickable icons for "Planner," "Mood Tracker," "Chill Space," "SOS," etc.
    *   **Purpose:** Fulfills the **`RecyclerView`/`GridView` requirement (f-i)**. It will use **Explicit Intents** to launch all other activities.
*   **Activity 2: Study-Life Balance Planner**
    *   **Function:** A simple calendar or list view where users can add, view, edit, and delete events (e.g., "Study BC13283," "Take a 30-min walk," "Group Meeting").
    *   **Database:** Implements full **CRUD (Create, Read, Update, Delete)** functionality with Firestore, saving events under the logged-in user's ID. Fulfills requirement **(f-iii)**.
*   **Sensor Implementation: Step Counter (Pedometer)**
    *   **Integration:** On the Planner page, add a "Daily Step Goal" feature. It will use the step counter sensor to display the user's current steps for the day against a goal they set. This promotes physical activity as a stress management tool.

---

#### **Member 2: Self-Assessment & Tracking**

This member focuses on features that help the user understand and log their mental state.

*   **Activity 1: Mood & Stress Tracker**
    *   **Function:** A daily check-in page. It will have an emoji slider for mood, another slider for stress level, and a multi-line `EditText` for a journal entry. A "Save" button will store this data in Firestore, timestamped and linked to the user.
    *   **Database:** Implements **Create** and **Read** (to potentially show past entries) from Firestore.
*   **Activity 2: Mental Health Quiz**
    *   **Function:** A simple, multi-page quiz (using `Fragments` or separate activities) for screening symptoms of anxiety (e.g., using questions from the GAD-7 scale). It will calculate and display a score at the end.
    *   **Purpose:** Provides a tangible self-assessment tool as required by the case study.
*   **Sensor Implementation: Proximity Sensor**
    *   **Integration:** Create a "Focus Mode" feature within the Planner (or as a separate simple activity linked from the dashboard). When a user starts a focus session, if they pick up their phone, the proximity sensor will detect the change (face is far from the screen). The app can then trigger a `Toast` message like, "Stay focused! You can do this."

---

#### **Member 3: Relaxation & Immediate Support**

This member focuses on providing in-app relaxation tools and emergency access.

*   **Activity 1: Chill Space**
    *   **Function:** A page with a `RecyclerView` listing calming audio tracks (e.g., "Rain Sounds," "5-Min Meditation"). Clicking an item will play the audio file (stored locally in the `res/raw` folder) using Android's `MediaPlayer`.
    *   **Purpose:** Fulfills the "Chill Space" requirement and can also use a **`RecyclerView`**.
*   **Activity 2: SOS Help Page**
    *   **Function:** A simple page with clear, large buttons.
        *   One button: "Call Campus Hotline." This will use an **`Implicit Intent`** (`Intent.ACTION_DIAL`) to open the phone's dialer with the university's emergency number pre-filled. This fulfills requirement **(f-ii)**.
        *   Another button: "Find Campus Counselling."
    *   **Purpose:** Provides direct access to help, as specified in the case study.
*   **Sensor Implementation: GPS / Location Sensor**
    *   **Integration:** On the SOS Help Page, the "Find Campus Counselling" button will use the location sensor to get the user's current location. It will then launch an **`Implicit Intent`** for Google Maps, showing the route from the user's current location to the pre-defined coordinates of the campus counselling center.

---

#### **Member 4: Community & Interactive Relaxation**

This member focuses on peer support and a more interactive wellness activity.

*   **Activity 1: Anonymous Chatroom**
    *   **Function:** A simple chat interface. It will have a `RecyclerView` to display messages and an `EditText` with a `Button` to send messages. All messages will be posted to a single "general" collection in Firestore. To keep it simple and anonymous, messages won't be tied to specific user accounts.
    *   **Database:** Implements **Create** (sending messages) and **Read** (displaying messages in real-time using Firestore's snapshot listeners).
*   **Activity 2: Guided Breathing Exercise**
    *   **Function:** A visual relaxation tool. It will feature an animation (e.g., a circle that expands and contracts) and text that changes ("Breathe In...", "Hold", "Breathe Out...") to guide the user through a calming breathing cycle. This can be done with simple Android animations.
    *   **Purpose:** Fulfills the "breathing guides" part of the "Chill Space" feature.
*   **Sensor Implementation: Accelerometer**
    *   **Integration:** Implement a "Shake for a Tip" feature on the Dashboard or within the Chill Space. When the user shakes their phone, the accelerometer will detect this motion and the app will display a random wellness tip or motivational quote in a `Toast` or `AlertDialog`.

---

### **4. How This Plan Meets the Assignment Criteria**

Let's double-check against the rubric (Criteria 1-3).

*   **`a-c` (Proposal - Intro, Problem, Objectives):** Your report will introduce the problem of student mental health in Malaysia, define the **scope** (our app focuses on stress management and routine building, not clinical therapy), and state the **limitations** (e.g., not a replacement for professional help, relies on user self-reporting). Your objectives will be:
    1.  To design and develop a mobile application with features for mood tracking, planning, and relaxation.
    2.  To integrate four mobile sensors (Pedometer, Proximity, GPS, Accelerometer) to create an interactive and context-aware user experience.
    3.  To implement a Firebase Firestore database for persistent data storage of user information, planner events, and mood logs.

*   **`d` (Methodology & Storyboard):**
    *   **Methodology:** You can choose an Agile-like approach. Break the 4-week duration into sprints. Week 1: Planning, UI/UX design (Storyboard), and Firebase setup. Week 2-3: Feature development by each member. Week 4: Integration, testing, bug fixing, and preparing deliverables.
    *   **Storyboard:** Use Figma or Canva to design the 10 screens and show the flow (e.g., Login -> Dashboard -> Planner).

*   **`f` (Development Requirements):**
    *   **`f-i` (Recycle/Grid View):** **Covered** (Dashboard, Chill Space, Chatroom).
    *   **`f-ii` (Intents):** **Covered**. **Explicit** (Dashboard to all other activities) and **Implicit** (SOS page to Dialer/Maps).
    *   **`f-iii` (Database):** **Covered**. Firestore is used for Register, Login, Planner (CRUD), and Mood Tracker.
    *   **`f-iv` (Sensors):** **Covered**. Four sensors are integrated meaningfully: Step Counter, Proximity, GPS, and Accelerometer.
    *   **`f-v` (Activities):** **Covered**. 10 total activities are planned, with each member responsible for 2 fully functional ones.
    *   **`f-vi` (No Errors):** This is your goal during testing.

*   **`g-h` (Group Work & Poster):**
    *   **Logbook:** Keep a simple Google Doc to log your weekly meetings, decisions made, problems faced (e.g., "Member 3 had trouble with GPS permissions, we solved it by..."), and assigned tasks.
    *   **Poster:** Design a poster for **CampusCalm** with your app logo, screenshots, a QR code to a demo video, and key features highlighted. Use an entrepreneurship-focused title like "CampusCalm: Your Partner in University Wellness."

This comprehensive plan gives your team a clear path forward. You have a well-defined app, a specific focus, and a clear distribution of work that meets every single requirement of the assignment. Good luck