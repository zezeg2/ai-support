# **AI-Support**

📌Maven Central
Repository: [Maven Central: io.github.zezeg2:ai-support:2.1.3](https://central.sonatype.com/artifact/io.github.zezeg2/ai-support)

## **About This module**

`AI-Support` 는 개발자가 언어모델을 통해 결과값을 얻을 수 있도록 하는 함수를 정의하고 인스턴스화 할 수 있도록 하는 자바 라이브러리 입니다.

## **Feature**

- `AI-suppport`는 OpenAI의 언어모델(GPT-3.5, GPT4)의 Chat Completion API를 호출을 추상화 하기 때문에 OpenAI의 API-Key를 필요로 합니다
- 해당 모듈은 SpringBoot-Starter 의존성을 가지며 스프링 부트 애플리케이션에서 자동 구성을 지원합니다.
- 해당 모듈은 synchronous 및 reactive stream 환경을 모두 지원하도록 설계되었으며 자동 구성을 통해 설정 가능합니다.
- `(Reactive)AIFunction`은 실행(execute) 시점(OpenAI API Call)에 언어모델을 파라미터로 전달할 수 있습니다.
- `(Reactive)AIFunction`은 개발자가 의도한 결과의 정확도를 보장하기 위해 `ValidatorChain`을 통해 결과값이 검증되고 수정됩니다.
    - 기본 제공 Validator : `JsonResultValidator`, `ConstrainValidator`
    - `Validator` 또한 내부적인 프롬프트를 설정하고 Chat Completion API를 호출합니다
    - `Validator` 인터페이스를 구현하여 개발자 정의의 검증 로직을 추가할 수 있습니다. (우선순위, 적용 대상 함수, 언어모델 지정 옵션을 어노테이션을 통해 제공)
- `MessageContext` 는 컨텍스트를 통해 메세지 저장을 지원합니다
    - 자동구성을 통해 `local`(default), `redis`, `mongo` 중 선택할 수 있습니다
    - 모든 함수 실행 및 검증 시 내부적으로 메세지 컨텍스트가 분리됩니다.
    - 메세지 컨텍스트는 식별자(default UUID), FunctionName, ValidatorName으로 쿼리 가능

## Concept Diagram

1. 스프링 컨텍스트로 부터 `(Reactive)AISupport` 인스턴스를 가져오고, 해당 인스턴스로부터 `(Reactive)AIFunction` 생성합니다.

![image](https://github.com/zezeg2/ai-support/assets/71999370/0f42f4b4-d015-440f-8fa4-2636e135a81c)


2. 생성된 `Reactive)AIFunction`  의 execute() 실행 시 동작

- ExecuteParameters는 AIFunction의 execute() 실행 파라미터로써 컨텍스트 식별자, 인풋 데이터, 실행 모델, 결과 예시 객체 필드를 가집니다.

![image](https://github.com/zezeg2/ai-support/assets/71999370/8d90c976-8f42-45df-9bdc-402d6d217ec5)


3. 함수 실행 내부 결과 검증 프로세스

![image](https://github.com/zezeg2/ai-support/assets/71999370/a3c78ec3-d221-4c6e-a861-8359afb0c2bb)


## Getting Started

### dependency

- maven

  ```xml
  <dependency>
      <groupId>io.github.zezeg2</groupId>
      <artifactId>ai-support</artifactId>
      <version>2.1.3</version>
  </dependency>
  ```

- gradle

  ```
  implementation 'io.github.zezeg2:ai-support:2.1.3'
  ```

### Properties

application.yaml

```yaml
ai-supporter:
  # OpenAI API 관련 property 설정입니다.
  api:
    token: "YOUR_OPENAI_API_KEY"
    # OpenAI API 호출의 타임아웃을 설정합니다
    timeout: 180
    # AIFunction, Validator가 사용할 기본 언어모델을 설정합니다.
    model: gpt_3_5_turbo
  # 컨텍스트 관련 설정입니다
  context:
    # 메세지 컨텍스트를 저장할 방법을 설정합니다.
    context: mongo
    # 애플리케이션의 실행 환경에 따라 synchronous/ reactive 를 선택합니다.
    environment: reactive
  # (실험중) 어노테이션 및 AOP 를 통해 메세지 브로커(Kafka Cluster)에 메세지 Publish를 활성화 및 비활성화  할 수 있습니다. 
  kafka-publish:
    enabled: false
```

## Usage Example

요리 레시피 제작을 위한 함수 생성 및 실행 예시 입니다.

CookRecipe.java

```java

@Getter
@Setter
@NoArgsConstructor
public class CookRecipe extends BaseSupportType {

    @FieldDesc("Cooking Ingredients List")
    List<Ingredient> ingredients;

    @KeyValueDesc(key = "number index", value = "Content of each step including cooking time")
    Map<Integer, String> cookStep;
}
```

Ingredient.java

```java

@Getter
@Setter
@NoArgsConstructor
public class Ingredient extends BaseSupportType {

    @FieldDesc("name of ingredient")
    String name;

    @FieldDesc("amount of ingredient")
    String amount;
}
```

> `BaseSupportType` 을 구현하는 클래스는 `@FiledDesc`, `@KeyValueDesc` 어노테이션을 필드에 적용하여 필드에 대한 설명을 추가하고 해당 정보를 언어모델에 전달할 수 있습니다.



(Reactive)TestService.java

```java

@Service
public class TestService {

    @Autowired
    private AISupport aiSupport;

    public CookRecipe execute() {
        String functionName = "createRecipe";
        String role = "Master Chef";
        String functionDescription = "Create a recipe with ingredients and cooking order given as input";
        List<Constraint> constraints = ConstraintsFactory.builder()
                .addConstraint("Language", "English")
                .build();
        AIFunction<CookRecipe> createRecipe = aiSupport.createFunction(CookRecipe.class,
                functionName,
                role,
                functionDescription,
                constraints);

        return createRecipe.execute(ExecuteParameters.<CookRecipe>builder()
                .identifier(Thread.currentThread().getName())
                .args(ArgumentsFactory.builder()
                        .addArgument("Spaghetti Napolitan", ArgumentDesc.builder()
                                .keyDesc("foodName")
                                .valueDesc("name of target food")
                                .build())
                        .build()
                )
//                .model(ModelMapper.map(Model.GPT_4))
//                .example(new CookRecipe())
                .build());
    }
}

@Service
public class ReactiveTestService {

    @Autowired
    private ReactiveAISupport aiSupport;

    public Mono<CookRecipe> execute() {
        String functionName = "createRecipe";
        String role = "Master Chef";
        String functionDescription = "Create a recipe with ingredients and cooking order given as input";
        List<Constraint> constraints = ConstraintsFactory.builder()
                .addConstraint("Language", "English")
                .build();

        ReactiveAIFunction<CookRecipe> createRecipe = aiSupport.createFunction(CookRecipe.class,
                functionName,
                role,
                functionDescription,
                constraints);

        return createRecipe.execute(ExecuteParameters.<CookRecipe>builder()
                .identifier(Thread.currentThread().getName())
                .args(ArgumentsFactory.builder()
                        .addArgument("Spaghetti Napolitan", ArgumentDesc.builder()
                                .keyDesc("foodName")
                                .valueDesc("name of target food")
                                .build())
                        .build()
                )
//                .model(ModelMapper.map(Model.GPT_4))
//                .example(new CookRecipe())
                .build()).doOnNext(System.out::println);
    }
}
```

> 제약조건(Constraints)는 `ConstraintsFactory`를 통해 생성할 수 있습니다(Using Builder).

> ExecuteParameters의 args 는 `ArgumentsFactory`를 통해 생성할 수 있습니다(Using Builder).



(Reactive)RecipeDetailValidator.java

```java

@Component
@ValidateTarget(names = {"createRecipe"}, order = 1)
public class RecipeDetailValidator extends ResultValidator {
    private final String RECIPE_DETAIL_VALIDATE_TEMPLATE = """
            1. Check that each step of the cooking process contains information about cooking time.
            """;

    public RecipeDetailValidator(PromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    @Override
    protected String addTemplateContents(String functionName, FeedbackMessageContext feedbackMessageContext) {
        return RECIPE_DETAIL_VALIDATE_TEMPLATE;
    }
}

@Component
@ValidateTarget(names = {"createRecipe"}, order = 1)
public class ReactiveRecipeDetailValidator extends ReactiveResultValidator {

    private final String RECIPE_DETAIL_VALIDATE_TEMPLATE = """
            1. Check that each element of the "cookStep" contains information about cooking time.
            """;

    public ReactiveRecipeDetailValidator(ReactivePromptManager promptManager, ObjectMapper mapper, OpenAIProperties openAIProperties) {
        super(promptManager, mapper, openAIProperties);
    }

    @Override
    protected Mono<String> addTemplateContents(String functionName, FeedbackMessageContext feedbackMessageContext) {
        return Mono.just(RECIPE_DETAIL_VALIDATE_TEMPLATE);
    }
}
```

> `Validator` 구현하여 커스텀 검증기를 등록하려면, 컴포넌트로 등록하고 `@ValidateTarget`  어노테이션을 작성 해줍니다. `addTemplateContents` 메서드를 오버라이드 하여
> Validator를 통해 검증하고자 하는 항목을 문자열로 반환하도록 합니다.



TestApplication.java

```java

@SpringBootApplication
@RequiredArgsConstructor
public class TestApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TestApplication.class, args);
//        TestService service = applicationContext.getBean("testService", TestService.class);
        ReactiveTestService service = applicationContext.getBean("reactiveTestService", ReactiveTestService.class);
//        System.out.println(service.execute());
        service.execute().subscribe(System.out::println);
    }
}
```

System Console(Synchronous case Only)

```
RecipeDetailValidator: Try Count: 1 ---------------------------------------------------------------------------
{
"ingredients": [
...
],
"cookStep": {
"1": "Cook the spaghetti according to the package instructions.",
"2": "In a large pan, heat the olive oil over medium heat.",
"3": "Add the bacon and sauté until crispy.",
"4": "Add the onion, green pepper, red pepper, and garlic. Sauté until the vegetables are tender.",
"5": "Add the ketchup and Worcestershire sauce. Stir until well combined.",
"6": "Season with salt and black pepper.",
"7": "Add the cooked spaghetti to the pan and mix well.",
"8": "Add the butter and stir until melted.",
"9": "Serve the spaghetti Napolitan with grated Parmesan cheese and chopped parsley on top."
}
}
Feedback on results exists
{
"valid": false,
"issueList": [
{
"issue": "Cooking time information is missing from the cookStep",
"solution": "Include cooking time information for each step of the cooking process in the cookStep"
}
]
}
RecipeDetailValidator: Try Count: 2 ---------------------------------------------------------------------------
...
(Updated Result here)
...
JsonResultValidator: Try Count: 1 ---------------------------------------------------------------------------
...
ConstraintsValidator: Try Count: 1 ---------------------------------------------------------------------------
...
{
"ingredients": [
{
"name": "spaghetti",
"amount": "200 grams"
}, {
"name": "bacon",
"amount": "100 grams"
}, {
"name": "onion",
"amount": "1"
}, {
"name": "green pepper",
"amount": "1"
}, {
"name": "red pepper",
"amount": "1"
}, {
"name": "garlic",
"amount": "2 cloves"
}, {
"name": "ketchup",
"amount": "3 tablespoons"
}, {
"name": "Worcestershire sauce",
"amount": "3 tablespoons"
}, {
"name": "olive oil",
"amount": "2 tablespoons"
}, {
"name": "salt",
"amount": "1/2 teaspoon"
}, {
"name": "black pepper",
"amount": "1/4 teaspoon"
}, {
"name": "butter",
"amount": "1 tablespoon"
}, {
"name": "grated Parmesan cheese",
"amount": "Parmesan cheese"
}, {
"name": "chopped parsley",
"amount": "Parsley"
}
],
"cookStep": {
"1": "Cook the spaghetti according to the package instructions. (Cooking time: 10 minutes)",
"2": "In a large pan, heat the olive oil over medium heat.",
"3": "Add the bacon and sauté until crispy. (Cooking time: 5 minutes)",
"4": "Add the onion, green pepper, red pepper, and garlic. Sauté until the vegetables are tender. (Cooking time: 8 minutes)",
"5": "Add the ketchup and Worcestershire sauce. Stir until well combined.",
"6": "Season with salt and black pepper.",
"7": "Add the cooked spaghetti to the pan and mix well.",
"8": "Add the butter and stir until melted.",
"9": "Serve the spaghetti Napolitan with grated Parmesan cheese and chopped parsley on top."
}
}
```

### Prompts

위 예시에서 생성된 프롬프트들(Functions Prompt, Validator Promt)은 다음과 같습니다. (기본 검증기 제외)

- createRecipe

  ```
  Please consider yourself as a(n) Master Chef. Your task is to execute the [Command] and generate a detailed, professional result in the [Result Format], using the input in the [Input Format].
  Please adhere to the "[Constraints]" given below.
  
  [Command]
  Create a recipe with ingredients and cooking order given as input
  
  [Constraints]
  - Language: English
  - Do not include any other explanatory text in your response.
  
  [Input Format]
  ```json
  {
    "foodName" : "name of target food"
  }
  [Result Format]
  {
    "ingredients" : [ {
      "amount" : "amount of ingredient",
      "name" : "name of ingredient"
    } ],
    "cookStep" : {
      "number index" : "Content of each step including cooking time"
    }
  }
  
  If feedback is provided in the "[Feedback Format]", adjust your previous results based on the content of the feedback. 
  [Feedback Format]
  {
    "valid" : "Boolean value indicating whether the given result is perfect (true) or not (false)",
    "issueList" : [ {
      "issue" : "detail of caught issue",
      "solution" : "Resolution of issue. this feedback will be utilize to improve next JSON result."
    } ]
  }
  ```


- createRecipe:RecipeDetailValidator

  ````
  Your task is to conduct a comprehensive inspection of the provided JSON data and identify issues.
  Perform an inspection solely according to the [Inspection Criteria], identify issues, then propose solutions to rectify these issues.
  After you've completed your assessment, please provide feedback using the [Result Format].
  
  [Inspection Criteria]
  1. Check that each step of the cooking process contains information about cooking time.
  
  
  [Input Format]
  ```json
  {
    "ingredients" : [ {
      "amount" : "amount of ingredient",
      "name" : "name of ingredient"
    } ],
    "cookStep" : {
      "number index" : "Content of each step including cooking time"
    }
  }
  ```
  
  [Result Format]
  ```json
  {
    "valid" : "Boolean value indicating whether the given result is perfect (true) or not (false)",
    "issueList" : [ {
      "issue" : "detail of caught issue",
      "solution" : "Resolution of issue. this feedback will be utilize to improve next JSON result."
    } ]
  }
  ```
  
  - Each element within the "issueList" should be concise and clear.
  - Do not include any other explanatory text in your response without the result.
  
  ````

  
