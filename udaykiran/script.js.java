const micBtn = document.getElementById("micBtn");

const statusEl = document.getElementById("status");

const liveText = document.getElementById("liveText");

const problemsList =
    document.getElementById("problemsList");


/* PROFILE ELEMENTS */

const menuBtn =
    document.getElementById("menuBtn");

const drawerBackdrop =
    document.getElementById("drawerBackdrop");

const closeDrawer =
    document.getElementById("closeDrawer");

const profileForm =
    document.getElementById("profileForm");

const nameInput =
    document.getElementById("name");

const mobileInput =
    document.getElementById("mobile");

const emailInput =
    document.getElementById("email");

const toast =
    document.getElementById("toast");


/* VARIABLES */

let recognition;

let isRecording = false;

let finalTranscript = "";


/* TOAST MESSAGE */

function showToast(message) {

    toast.textContent = message;

    toast.classList.add("show");

    setTimeout(() => {

        toast.classList.remove("show");

    }, 2400);

}


/* WELCOME VOICE */

function speakWelcome() {

    if (!("speechSynthesis" in window)) {
        return;
    }

    const message =
        new SpeechSynthesisUtterance(
            "Welcome to Public Problems App"
        );

    message.lang = "en-IN";

    message.rate = 0.95;

    message.pitch = 1.12;


    const voices =
        speechSynthesis.getVoices();


    const voice =
        voices.find(v =>
            /female|zira|samantha|victoria|google uk english female/i
            .test(v.name)
        )
        ||
        voices.find(v =>
            /en-IN|en-GB|en-US/i
            .test(v.lang)
        );


    if (voice) {

        message.voice = voice;

    }


    speechSynthesis.cancel();

    speechSynthesis.speak(message);

}


/* WHEN APP OPENS */

window.addEventListener("load", () => {

    loadProfile();

    renderProblems();

    setTimeout(() => {

        speakWelcome();

    }, 500);

});


/* GET SAVED PROBLEMS */

function getProblems() {

    return JSON.parse(

        localStorage.getItem(
            "publicProblems"
        ) || "[]"

    );

}


/* SAVE PROBLEM */

function saveProblem(text) {

    const problems = getProblems();


    const problemNumber =
        problems.length + 1;


    problems.push({

        id: problemNumber,

        title:
            `Problem ${problemNumber}`,

        text: text,

        createdAt:
            new Date().toLocaleString()

    });


    localStorage.setItem(

        "publicProblems",

        JSON.stringify(problems)

    );


    renderProblems();

}


/* DISPLAY PROBLEMS */

function renderProblems() {

    const problems =
        getProblems();


    if (problems.length === 0) {

        problemsList.innerHTML =
            `
            <div class="empty">
                No problems recorded yet.
            </div>
            `;

        return;

    }


    problemsList.innerHTML =

        problems
            .slice()
            .reverse()
            .map(problem =>

                `
                <article class="problem">

                    <strong>
                        ${escapeHTML(problem.title)}
                    </strong>

                    <p>
                        ${escapeHTML(problem.text)}
                    </p>

                </article>
                `

            )
            .join("");

}


/* SECURITY */

function escapeHTML(value) {

    return String(value)

        .replaceAll("&", "&amp;")

        .replaceAll("<", "&lt;")

        .replaceAll(">", "&gt;")

        .replaceAll('"', "&quot;")

        .replaceAll("'", "&#039;");

}


/* SPEECH RECOGNITION */

function setupSpeechRecognition() {

    const SpeechRecognition =
        window.SpeechRecognition ||
        window.webkitSpeechRecognition;


    if (!SpeechRecognition) {

        return false;

    }


    recognition =
        new SpeechRecognition();


    /*
       Browser speech recognition
       converts speech to text.

       This version is configured for
       English speech.
    */

    recognition.lang = "en-IN";

    recognition.continuous = true;

    recognition.interimResults = true;


    /* START */

    recognition.onstart = () => {

        isRecording = true;

        finalTranscript = "";


        micBtn.classList.add(
            "recording"
        );


        micBtn.textContent = "■";


        statusEl.textContent =
            "Recording... tap again to stop";


        liveText.textContent =
            "Listening...";

    };


    /* SPEECH RESULT */

    recognition.onresult = event => {

        let interimTranscript = "";


        for (
            let i = event.resultIndex;
            i < event.results.length;
            i++
        ) {

            const transcript =
                event.results[i][0].transcript;


            if (
                event.results[i].isFinal
            ) {

                finalTranscript +=
                    transcript + " ";

            }
            else {

                interimTranscript +=
                    transcript;

            }

        }


        liveText.textContent =

            (
                finalTranscript +
                interimTranscript
            ).trim()
            ||
            "Listening...";

    };


    /* ERROR */

    recognition.onerror = event => {

        isRecording = false;


        micBtn.classList.remove(
            "recording"
        );


        micBtn.textContent = "🎙️";


        if (
            event.error === "not-allowed"
        ) {

            statusEl.textContent =
                "Microphone permission denied";


            showToast(
                "Please allow microphone permission"
            );

        }
        else {

            statusEl.textContent =
                "Could not record. Try again.";

        }

    };


    /* END */

    recognition.onend = () => {

        if (!isRecording) {

            finishRecording();

        }

    };


    return true;

}


/* TRANSLATE TO ENGLISH */

async function translateToEnglish(text) {

    /*
       IMPORTANT:

       This function currently returns
       the recognized text.

       For Telugu/Hindi/Tamil/etc.
       to English translation, you need
       a translation API/backend.

       Example:

       Telugu speech
          ↓
       Speech-to-text
          ↓
       Translation API
          ↓
       English text
    */


    return text;

}


/* FINISH RECORDING */

async function finishRecording() {

    const spokenText =
        finalTranscript.trim();


    micBtn.classList.remove(
        "recording"
    );


    micBtn.textContent = "🎙️";


    /* NO SPEECH */

    if (!spokenText) {

        statusEl.textContent =
            "No speech detected. Try again.";

        liveText.textContent =
            "Your converted English text will appear here.";

        return;

    }


    statusEl.textContent =
        "Converting to English...";


    /* TRANSLATION */

    const englishText =
        await translateToEnglish(
            spokenText
        );


    /* SHOW TEXT */

    liveText.textContent =
        englishText;


    /* SAVE */

    saveProblem(
        englishText
    );


    const count =
        getProblems().length;


    statusEl.textContent =
        `Saved as Problem ${count}`;


    showToast(
        `Problem ${count} saved`
    );

}


/* MICROPHONE BUTTON */

micBtn.addEventListener(
    "click",
    () => {


        /* FIRST TIME */

        if (!recognition) {

            const supported =
                setupSpeechRecognition();


            if (!supported) {

                statusEl.textContent =
                    "Speech recognition is not supported.";

                showToast(
                    "Please use Google Chrome"
                );

                return;

            }

        }


        /* START */

        if (!isRecording) {

            try {

                recognition.start();

            }
            catch (error) {

                console.log(error);

            }

        }


        /* STOP */

        else {

            isRecording = false;

            statusEl.textContent =
                "Processing recording...";

            recognition.stop();

        }

    }
);


/* OPEN PROFILE */

menuBtn.addEventListener(
    "click",
    () => {

        drawerBackdrop.classList.add(
            "open"
        );

    }
);


/* CLOSE PROFILE */

closeDrawer.addEventListener(
    "click",
    () => {

        drawerBackdrop.classList.remove(
            "open"
        );

    }
);


/* CLOSE WHEN CLICKING OUTSIDE */

drawerBackdrop.addEventListener(
    "click",
    event => {

        if (
            event.target ===
            drawerBackdrop
        ) {

            drawerBackdrop.classList.remove(
                "open"
            );

        }

    }
);


/* SAVE PROFILE */

profileForm.addEventListener(
    "submit",
    event => {

        event.preventDefault();


        const profile = {

            name:
                nameInput.value.trim(),

            mobile:
                mobileInput.value.trim(),

            email:
                emailInput.value.trim()

        };


        if (
            !profile.name ||
            !profile.mobile ||
            !profile.email
        ) {

            showToast(
                "Please fill all fields"
            );

            return;

        }


        localStorage.setItem(

            "publicProblemsProfile",

            JSON.stringify(profile)

        );


        showToast(
            "Profile saved"
        );


        drawerBackdrop.classList.remove(
            "open"
        );

    }
);


/* LOAD PROFILE */

function loadProfile() {

    const profile =
        JSON.parse(

            localStorage.getItem(
                "publicProblemsProfile"
            ) || "null"

        );


    if (!profile) {

        return;

    }


    nameInput.value =
        profile.name || "";


    mobileInput.value =
        profile.mobile || "";


    emailInput.value =
        profile.email || "";

}