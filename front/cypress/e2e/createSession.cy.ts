describe('Session creation as admin spec', () => {
  let user = {
    id: 3,
    email: "yoga@studio.com",
    password: "test!1234",
    firstName: "Tony",
    lastName: "Name",
    admin: true
  }

  let allSession = [
    {
        "id": 1,
        "name": "sessionName123",
        "date": "2025-10-16T00:00:00.000+00:00",
        "teacher_id": 1,
        "description": "''",
        "users": [
            2
        ],
        "createdAt": "2025-10-16T13:28:28",
        "updatedAt": "2025-10-16T13:28:28"
    }
  ]

  let createdTeacher = {
      "id": 2,
      "name": "fff",
      "date": "2025-10-15T00:00:00.000+00:00",
      "teacher_id": 1,
      "description": "fff",
      "users": [],
      "createdAt": "2025-10-21T21:22:16.2621871",
      "updatedAt": "2025-10-21T21:22:16.2621871"
  }

  let teacher = {
    "id": 1,
    "lastName": "DELAHAYE",
    "firstName": "Margot",
    "createdAt": "2025-10-16T13:18:17",
    "updatedAt": "2025-10-16T13:18:17"
  }

  function selectFirstSession() {
    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/' + allSession[0].id,
      },
      allSession[0]
    ).as('session');
    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/' + allSession[0].teacher_id,
      },
      teacher
    ).as('session')

    cy.contains('span', 'Detail').click();
  }

  function mockBeforeHome() {
    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      allSession
    ).as('session')
  }

  it('Login successful', () => {
    cy.visit("/login");
    cy.intercept('POST', '/api/auth/login', {
      body: {
        id: user.id,
        username: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        admin: user.admin
      },
    })

    mockBeforeHome();

    cy.get('input[formControlName=email]').type(user.email)
    cy.get('input[formControlName=password]').type(`${user.password}{enter}{enter}`)

    cy.url().should('include', '/sessions');
  })

  it('Session creation', () => {
    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher',
      },
      [teacher]
    )

    cy.contains('span', 'Create').click();

    cy.get('input[formControlName=name]').type("test")
    cy.get('input[formControlName=date]').type("2000-01-02")
    cy.get('mat-select[formControlName=teacher_id]').click()
    cy.get('mat-option').contains('Margot DELAHAYE').click()
    cy.get('textarea[formControlName=description]').type("desc")

    cy.intercept(
      {
        method: 'POST',
        url: '/api/session',
      },
      createdTeacher
    )

    cy.contains('span', 'Save').click();
  })
});