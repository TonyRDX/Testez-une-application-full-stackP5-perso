# Install & misc
Follow each README or check package.json commands.
JDK version : 8
Spring version : 2.6
Angular version : 14.2
Node version : 16

# Back testing
mvn clean test
Coverage file : back/target/site/jacoco/index.html

# Front Testing
## Unit / Jest
npm run test
Coverage file : front/coverage/jest/lcov-report/index.html

## E2E run fast CLI test (to update coverage)
npm run e2e:ci
Coverage file : front/coverage/lcov-report/index.html
