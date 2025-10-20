import { ComponentFixture, fakeAsync, flush, flushMicrotasks, TestBed } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { SessionService } from 'src/app/services/session.service';
import { UserService } from 'src/app/services/user.service';

import { MeComponent } from './me.component';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('MeComponent', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;
  let sessionService: SessionService;
  let userService: UserService;
  let snackBar: MatSnackBar;
  let httpMock: HttpTestingController;
  
  beforeEach(async () => {
    jest.clearAllMocks();

    await TestBed.configureTestingModule({
      declarations: [MeComponent],
      imports: [
        MatSnackBarModule,
        HttpClientTestingModule,
        NoopAnimationsModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        RouterTestingModule
      ],
      providers: [
        SessionService,
        UserService,
        { provide: Router, useValue: { navigate: jest.fn() } }, 
      ],
    })
      .compileComponents();
        
    sessionService = TestBed.inject(SessionService);
    sessionService.logIn({ id: 42, admin: true } as SessionInformation);
    userService = TestBed.inject(UserService);
    snackBar = TestBed.inject(MatSnackBar);
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;
    
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should back', () => {
    const backSpy = jest.spyOn(window.history, 'back').mockImplementation(() => {});
    component.back();
    expect(backSpy).toHaveBeenCalled();
    backSpy.mockRestore();
  });

  it('should call delete(), open snackbar, logout and navigate home', fakeAsync(() => {
    const router = TestBed.inject(Router);
    const navigateSpy = jest.spyOn(router, 'navigate');
    const sessionServiceLogOut = jest.spyOn(sessionService, 'logOut');
    const snackBarOpen = jest.spyOn(snackBar, 'open');

    component.delete();

    const req1 = httpMock.expectOne(
      r => r.method === 'DELETE' 
      && r.url.includes('api/user/42')
    );
    req1.flush({ success: true });
    expect(snackBarOpen).toHaveBeenCalled();
    expect(sessionServiceLogOut).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);

    flushMicrotasks();
    flush();
  }));
});
