describe('User spec (register, login & account)', () => {
  let data = {
    id: 1,
    email: "yoga@studio.com",
    password: "test!1234",
    firstName: "Tony",
    lastName: "Name"
  }

  it("Register successful", () => {
    cy.intercept('POST', '/api/auth/register',  {
      statusCode: 200,
      body: {
        users: [{message: "User registered successfully!"}]
      }
    });

    cy.viewport(1111, 835);
    cy.visit("/");
    cy.get("span:nth-of-type(2)").click();
    cy.get("#mat-input-0").type(data.firstName);
    cy.get("#mat-input-1").type(data.lastName);
    cy.get("#mat-input-2").type(data.email);
    cy.get("#mat-input-3").type(data.password);
    cy.get("button").click();

    cy.url().should('include', '/login');
  });

  it('Login successful', () => {
    cy.intercept('POST', '/api/auth/login', {
      body: {
        id: data.id,
        username: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        admin: true
      },
    })

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      []
    ).as('session')

    cy.get('input[formControlName=email]').type(data.email)
    cy.get('input[formControlName=password]').type(`${data.password}{enter}{enter}`)

    cy.url().should('include', '/sessions')
  })

  it('Account display', () => {
    cy.intercept('GET', '/api/user/' + data.id, {
      body: {
        "id": 1,
        "email": data.email,
        "lastName": data.lastName,
        "firstName": data.firstName,
        "admin": true,
        "createdAt": "2025-10-16T13:18:17",
        "updatedAt": "2025-10-16T13:18:17"
      },
    })

    cy.contains('span', 'Account').click()
    cy.url().should('include', '/me')
    cy.contains(data.firstName)
    cy.contains(data.email)
    cy.contains(data.lastName)
    cy.contains(data.password).should('not.exist')
  })
});