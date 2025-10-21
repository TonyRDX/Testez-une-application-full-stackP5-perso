describe('Session spec', () => {
  let user = {
    id: 3,
    email: "yoga@studio.com",
    password: "test!1234",
    firstName: "Tony",
    lastName: "Name",
    admin: false
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

  function mockBeforeParticipate(session: any) {
    cy.intercept(
      {
        method: 'POST',
        url: `/api/session/${session.id}/participate/${user.id}`,
      },
      createdTeacher
    );
    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/' + session.id,
      },
      { ...session, users: [...session.users, user.id] }
    );
    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/' + session.teacher_id,
      },
      teacher
    );
  }

  function mockBeforeUnparticipate(session: any) {
    cy.intercept(
      {
        method: 'DELETE',
        url: `/api/session/${session.id}/participate/${user.id}`,
      },
      createdTeacher
    );
    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/' + session.id,
      },
      session
    );
    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/' + session.teacher_id,
      },
      teacher
    );
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

  it('Home displays a session', () => {
    cy.contains(allSession[0].name);

    selectFirstSession();

    cy.url().should('include', '/sessions/detail/' + allSession[0].id);
  })

  it('Session detail', () => {
    let session = allSession[0];

    cy.contains(new RegExp(session.name, 'i')); // case insensitive
    cy.contains(session.users.length + " attendees");
  })

  it('Participate / unparticipate', () => {
    let session = allSession[0];

    mockBeforeParticipate(session);
    
    cy.contains('span', 'Participate').click();
    cy.contains('span', 'Do not participate');
    cy.contains((session.users.length + 1) + " attendees");

    mockBeforeUnparticipate(session);

    cy.contains('span', 'Do not participate').click();
    cy.contains('span', 'Participate');
    cy.contains(session.users.length + " attendees");
  })

  it('Session back', () => {
    mockBeforeHome();

    cy.contains('mat-icon', 'arrow_back').click();
    cy.url().should('include', '/sessions');

    //selectFirstSession();
  })
});

