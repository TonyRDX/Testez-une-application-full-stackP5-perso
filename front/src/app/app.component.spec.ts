import { HttpClientModule } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterTestingModule } from '@angular/router/testing';
import { expect } from '@jest/globals';

import { AppComponent } from './app.component';
import { Router } from '@angular/router';
import { SessionService } from './services/session.service';
import { SessionInformation } from './interfaces/sessionInformation.interface';
import { take } from 'rxjs';

describe('AppComponent', () => {
  let sessionService: SessionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientModule,
        MatToolbarModule
      ],
      declarations: [
        AppComponent
      ],
      providers: [
        SessionService,
      ]
    }).compileComponents();

    sessionService = TestBed.inject(SessionService);
    sessionService.logIn({ id: 42, admin: true } as SessionInformation);
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should log out and redirect', () => {
    const router = TestBed.inject(Router);
    const navigateSpy = jest.spyOn(router, 'navigate');
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    const isLoggedStates: boolean[] = [];
    const sub = app.$isLogged().pipe(take(2)).subscribe(v => isLoggedStates.push(v));

    app.logout();

    expect(isLoggedStates).toEqual([true, false]);
    expect(navigateSpy).toHaveBeenCalledWith(['']);

    sub.unsubscribe();
  });
});
