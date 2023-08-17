# **AI-Support**

[한국어 버전 보기](README-ko.md)

## **Maven Central Repository**

📌 [Maven Central: io.github.zezeg2:ai-support:2.1.3](https://central.sonatype.com/artifact/io.github.zezeg2/ai-support)

## **Overview**

`AI-Support` is a Java library enabling developers to define and instantiate functions that retrieve results from
language models.

## **Features**

1. **OpenAI Language Model Integration**:
   - Abstracts the invocation of OpenAI's language models (eg.,GPT-3.5, GPT4) via the Chat Completion API.
   - Requires an OpenAI API-Key.
2. **Spring Boot Compatibility**:
   - Has a dependency on SpringBoot-Starter.
   - Supports auto-configuration within Spring Boot applications.
3. **Flexible Execution**:
   - Designed to support both synchronous and reactive stream environments.
   - Configuration available via auto-configuration.
4. **Configurable Execution**:
   - During the execution (OpenAI API Call), you can pass the desired language model as a parameter
     using `(Reactive)AIFunction`.
5. **Result Validation**:
   - Ensures the accuracy of the intended result via `ValidatorChain`.
   - Built-in Validators: `JsonResultValidator`, `ConstrainValidator`.
   - Validators also set internal prompts and call the Chat Completion API.
   - Implement the `Validator` interface to add custom validation logic.
6. **Message Context Management**:
   - Supports message storage through context.
   - Choose from `local` (default), `redis`, or `mongo` via auto-configuration.
   - Queries possible by identifier (default UUID), FunctionName, ValidatorName.

## **Concept Diagrams**

1. Acquire the `(Reactive)AISupport` instance from the Spring context and spawn `(Reactive)AIFunction` using this
   instance.

![image1](https://github.com/zezeg2/ai-support/assets/71999370/0f42f4b4-d015-440f-8fa4-2636e135a81c)

2. Design and register validators applicable to `(Reactive)AIFunction`.

![image2](https://github.com/zezeg2/ai-support/assets/71999370/7d291dd5-506c-4c16-9606-b855b4692afc)

3. The process flow when invoking the `execute()` method of the established `(Reactive)AIFunction`.

![image3](https://github.com/zezeg2/ai-support/assets/71999370/8d90c976-8f42-45df-9bdc-402d6d217ec5)

4. Internal verification process of the function execution result.

![image4](https://github.com/zezeg2/ai-support/assets/71999370/a3c78ec3-d221-4c6e-a861-8359afb0c2bb)

## **Getting Started**

### Dependencies

**Maven**:

```xml

<dependency>
   <groupId>io.github.zezeg2</groupId>
   <artifactId>ai-support</artifactId>
   <version>2.1.3</version>
</dependency>
```

**Gradle**:

```groovy
implementation 'io.github.zezeg2:ai-support:2.1.3'
```

### Configuration

Add the following to your `application.yaml`:

**application.yaml**:

```yaml
ai-supporter:
   # Property settings related to the OpenAI API.
   api:
      token: "YOUR_OPENAI_API_KEY"
      # Set the timeout for OpenAI API calls.
      timeout: 180
      # Set the default language model to be used by AIFunction and Validator.
      model: gpt_3_5_turbo
   # Settings related to context.
   context:
      # Configure the method for storing message context.
      context: mongo
      # Choose synchronous/reactive based on the application's runtime environment.
      environment: reactive
   # (Experimental) Activate or deactivate message publishing to the message broker (Kafka Cluster) through annotations and AOP.
   kafka-publish:
      enabled: false

spring:
        # Add spring-data configuration according to context option
   ...
```

## **Usage Examples**

Below is a demonstration of function creation and execution to generate cooking recipes.

**CookRecipe.java**

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

**Ingredient.java**

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

> Note: Classes implementing `BaseSupportType` can use `@FiledDesc` and `@KeyValueDesc` annotations to describe fields.
> This information is then conveyed to the language model.

**TestService.java**

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
```

> Tip: Constraints can be set using the `ConstraintsFactory`. The `ArgumentsFactory` facilitates the creation of `Args`
> for `ExecuteParameters`.

**RecipeDetailValidator.java**

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
```

> To use a custom validator by implementing `Validator`, register it as a component and apply the `@ValidateTarget`
> annotation. Override the `addTemplateContents` method to specify validation components.

**TestApplication.java**

```java
@SpringBootApplication
@RequiredArgsConstructor
public class TestApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TestApplication.class, args);
       TestService service = applicationContext.getBean("testService", TestService.class);
       System.out.println(service.execute());
    }
}
```

**System Console Output (For synchronous execution only)**:

```plaintext
RecipeDetailValidator: Try Count: 1 ---------------------------------------------------------------------------
{
  "ingredients" : [
    ...
  ],
  "cookStep" : {
    "1" : "Cook the spaghetti according to the package instructions.",
    "2" : "In a large pan, heat the olive oil over medium heat.",
    "3" : "Add the bacon and sauté until crispy.",
    "4" : "Add the onion, green pepper, red pepper, and garlic. Sauté until the vegetables are tender.",
    "5" : "Add the ketchup and Worcestershire sauce. Stir until well combined.",
    "6" : "Season with salt and black pepper.",
    "7" : "Add the cooked spaghetti to the pan and mix well.",
    "8" : "Add the butter and stir until melted.",
    "9" : "Serve the spaghetti Napolitan with grated Parmesan cheese and chopped parsley on top."
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
  "ingredients" : [ {
    "name" : "spaghetti",
    "amount" : "200 grams"
  }, {
    "name" : "bacon",
    "amount" : "100 grams"
  }, {
    "name" : "onion",
    "amount" : "1"
  }, {
    "name" : "green pepper",
    "amount" : "1"
  }, {
    "name" : "red pepper",
    "amount" : "1"
  }, {
    "name" : "garlic",
    "amount" : "2 cloves"
  }, {
    "name" : "ketchup",
    "amount" : "3 tablespoons"
  }, {
    "name" : "Worcestershire sauce",
    "amount" : "3 tablespoons"
  }, {
    "name" : "olive oil",
    "amount" : "2 tablespoons"
  }, {
    "name" : "salt",
    "amount" : "1/2 teaspoon"
  }, {
    "name" : "black pepper",
    "amount" : "1/4 teaspoon"
  }, {
    "name" : "butter",
    "amount" : "1 tablespoon"
  }, {
    "name" : "grated Parmesan cheese",
    "amount" : "Parmesan cheese"
  }, {
    "name" : "chopped parsley",
    "amount" : "Parsley"
  } ],
  "cookStep" : {
    "1" : "Cook the spaghetti according to the package instructions. (Cooking time: 10 minutes)",
    "2" : "In a large pan, heat the olive oil over medium heat.",
    "3" : "Add the bacon and sauté until crispy. (Cooking time: 5 minutes)",
    "4" : "Add the onion, green pepper, red pepper, and garlic. Sauté until the vegetables are tender. (Cooking time: 8 minutes)",
    "5" : "Add the ketchup and Worcestershire sauce. Stir until well combined.",
    "6" : "Season with salt and black pepper.",
    "7" : "Add the cooked spaghetti to the pan and mix well.",
    "8" : "Add the butter and stir until melted.",
    "9" : "Serve the spaghetti Napolitan with grated Parmesan cheese and chopped parsley on top."
  }
}
```

### **Generated Prompts**:

The prompts generated in the above example (excluding default validators) are:

**createRecipe**:

````plaintext
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
````

**createRecipe: RecipeDetailValidator**:

````plaintext
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


