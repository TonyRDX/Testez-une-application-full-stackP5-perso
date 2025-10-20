import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DetailComponent } from './detail.component';
import { SessionService } from '../../../../services/session.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder } from '@angular/forms';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';

class ActivatedRouteMock {
  snapshot = { paramMap: { get: () => '123' } };
}
class RouterMock { navigate = jest.fn(); }
class MatSnackBarMock { open = jest.fn(); }

describe('DetailComponent', () => {
  let component: DetailComponent;
  let httpMock: HttpTestingController;
  let router: RouterMock;
  let snack: MatSnackBarMock;
  let sessionService: SessionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [DetailComponent],
      providers: [
        SessionService,
        { provide: ActivatedRoute, useClass: ActivatedRouteMock },
        { provide: Router, useClass: RouterMock },
        { provide: MatSnackBar, useClass: MatSnackBarMock },
        FormBuilder
      ],
    }).compileComponents();

    
    sessionService = TestBed.inject(SessionService);
    sessionService.logIn({ id: 42, admin: true } as SessionInformation);
    const fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router) as any;
    snack = TestBed.inject(MatSnackBar) as any;

    fixture.detectChanges();
  });

  it('should create', () => {
    const req = httpMock.expectOne(
      r => r.method === 'GET' 
      && r.url.includes('/session/123')
    );
    req.flush({});
    expect(component).toBeTruthy();
  })

  it('should back', () => {
    const backSpy = jest.spyOn(window.history, 'back').mockImplementation(() => {});
    component.back();
    expect(backSpy).toHaveBeenCalled();
    backSpy.mockRestore();
  });

  it('should request DELETE, and send snackbar', () => {
    component.delete();

    const req = httpMock.expectOne(
      r => r.method === 'DELETE' 
      && r.url.includes('/session/123')
    );
    req.flush({});

    expect(snack.open).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['sessions']);
  });

  it('should request POST, and send snackbar', () => {
    component.participate();

    const req = httpMock.expectOne(
      r => r.method === 'POST' 
      && r.url.includes('/session/123/participate/42')
    );
    req.flush({});
  });

  it('should request DELETE, and send snackbar', () => {
    component.unParticipate();

    const req = httpMock.expectOne(
      r => r.method === 'DELETE' 
      && r.url.includes('/session/123/participate/42')
    );
    req.flush({});
  });
});
