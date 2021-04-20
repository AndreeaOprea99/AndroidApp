package com.example.coolturaquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.coolturaquiz.SetsActivity.category_id;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView question, qCount, timer;
    private Button option1, option2, option3, option4;
    private List<Question> questionList;
    private int quesNum;
    private CountDownTimer countDown;
    private int score;
    private FirebaseFirestore firestore;
    private int setNr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        question = findViewById(R.id.question);
        qCount = findViewById(R.id.Question_number);
        timer = findViewById(R.id.countdown);

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);

        setNr=getIntent().getIntExtra("SETNR",1);
        firestore = FirebaseFirestore.getInstance();

        getQuestionsList();
        score = 0;
    }

    private void getQuestionsList() {
        questionList = new ArrayList<>();
        firestore.collection("CoolturaQuiz").document("CAT"+ String.valueOf(category_id))
                .collection("SET"+ String.valueOf(setNr))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    public void onComplete(@NotNull Task<QuerySnapshot> task) {
                         if(task.isSuccessful())
                         {
                              QuerySnapshot questions = task.getResult();

                              for(QueryDocumentSnapshot doc: questions){
                                  questionList.add(new Question(
                                          doc.getString("QUESTION"),
                                          doc.getString("A"),
                                          doc.getString("B"),
                                          doc.getString("C"),
                                          doc.getString("D"),
                                          Integer.valueOf(doc.getString("ANSWER"))
                                          ));
                              }

                             setQuestion();//call setQuestion

                            }
                            else{
                                Toast.makeText(QuestionActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
        });

    }

    private void setQuestion()
    {
        timer.setText(String.valueOf(10));

        question.setText(questionList.get(0).getQuestion());
        option1.setText(questionList.get(0).getOptionA());
        option2.setText(questionList.get(0).getOptionB());
        option3.setText(questionList.get(0).getOptionC());
        option4.setText(questionList.get(0).getOptionD());


        qCount.setText(String.valueOf(1) + "/" + String.valueOf(questionList.size()));

        startTimer();

        quesNum = 0;

    }

    //crearea timerului
    private void startTimer()
    {
        countDown = new CountDownTimer(32000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished < 30000)
                    timer.setText(String.valueOf(millisUntilFinished / 1000));
                if(millisUntilFinished < 30000)
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            @Override
            public void onFinish() {
                changeQuestion();
            }
        };
        countDown.start();

    }
    @Override
    public void onClick(View v)
    {
        int selectedOpt = 0;

        switch (v.getId())
        {
            case R.id.option1:
                selectedOpt = 1;
                break;

            case R.id.option2:
                selectedOpt = 2;
                break;
            case R.id.option3:
                selectedOpt = 3;
                break;

            case R.id.option4:
                selectedOpt = 4;
                break;

            default:

    }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    countDown.cancel();
    checkAnswer(selectedOpt, v);

    }
    private  void checkAnswer(int selectedOpt, View view){
        if (selectedOpt == questionList.get(quesNum).getCorrectAnswer())
        {
            //raspunsul corect a fost ales->schimb culoarea in verde

            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            score++;


        }
        else {
            //raspunsul e gresit->schimb culoarea in rosu
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            switch (questionList.get(quesNum).getCorrectAnswer())
            {
                case 1:
                    option1.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 2:
                    option2.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 3:
                    option3.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 4:
                    option4.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;

            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    changeQuestion();
                }
            }, 2000);
        }


//intrebarea se va schimba dupa 2 secunde dupa ce se afiseaza raspunsul corect

    }

    private void changeQuestion()
    {
        if( quesNum < questionList.size() - 1)
        {
            quesNum++;

            playAnim(question,0,0);
            playAnim(option1,0,1);
            playAnim(option2,0,2);
            playAnim(option3,0,3);
            playAnim(option4,0,4);

            qCount.setText(String.valueOf(quesNum+1) + "/" + String.valueOf(questionList.size()));

            timer.setText(String.valueOf(10));
            startTimer();

        }
        else
        {
            //ScoreActivity
            Intent intent = new Intent(QuestionActivity.this, ScoreActivity.class);
            intent.putExtra("SCORE", String.valueOf(score)+"/"+String.valueOf(questionList.size()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //QuestionActivity.this.finish();//e si ultima activitate

        }
    }

    private void playAnim(View view, final int value, int viewNumber)
    {
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500)
                .setStartDelay(100).setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(value == 0)
                        {
                            switch (viewNumber){
                                case 0:
                                    ((TextView)view).setText(questionList.get(quesNum).getQuestion());
                                    break;
                                case 1:
                                    ((Button)view).setText(questionList.get(quesNum).getOptionA());
                                    break;
                                case 2:
                                    ((Button)view).setText(questionList.get(quesNum).getOptionB());
                                    break;
                                case 3:
                                    ((Button)view).setText(questionList.get(quesNum).getOptionC());
                                    break;
                                case 4:
                                    ((Button)view).setText(questionList.get(quesNum).getOptionD());
                                    break;
                            }

                            if(viewNumber!=0){
                                ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF018786")));
                            }

                            playAnim(view,1, viewNumber);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

    }

    @Override
    //cand se apasa butonul de intoarcere timerul se opreste
    public void onBackPressed() {
        super.onBackPressed();

        countDown.cancel();
    }
}