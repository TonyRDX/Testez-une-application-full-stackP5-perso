import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {  FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { expect } from '@jest/globals';
import { SessionService } from 'src/app/services/session.service';
import { SessionApiService } from '../../services/session-api.service';

import { FormComponent } from './form.component';
import { of } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';

describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
  let mockSessionService: any;
  let mockSnackBar: any;
  let mockActivatedRoute: any;
  let mockRouter: any;
  let mockSessionApiService: any;

  beforeEach(async () => {
    mockSessionService = {
      sessionInformation: {
        admin: true
      }
    };

    mockSnackBar = {
      open: jest.fn()
    };

    mockSessionApiService = {
      create: jest.fn().mockReturnValue(of({})),
      update: jest.fn().mockReturnValue(of({})),
      detail: jest.fn().mockReturnValue(of({}))
    };

    mockActivatedRoute = { 
      snapshot: { 
        paramMap: new Map([['id', '123']]) 
      } 
    };

    mockRouter = {
      navigate: jest.fn().mockResolvedValue(true),
      url: '/sessions/create',
    };

    await TestBed.configureTestingModule({
      imports: [
        HttpClientModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule, 
        MatSnackBarModule,
        MatSelectModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: mockRouter}, 
      ],
      declarations: [FormComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should call create, open snackbar, and redirect', () => {
    fixture.detectChanges();

    component.submit();
    expect(mockSessionApiService.create).toHaveBeenCalled();
    expect(mockSnackBar.open).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalled();
  });

  it('should call update, open snackbar, and redirect', () => {
    const fb = TestBed.inject(FormBuilder);
    let mockFb = fb.group({
      name: ['Angular', Validators.required],
      date: ['2025-01-01', Validators.required],
      teacher_id: ['t1', Validators.required],
      description: ['Desc', [Validators.required, Validators.max(2000)]],
    });
    mockRouter.url = '/sessions/update';
    mockSessionApiService.update = jest.fn().mockReturnValue(
      of({mockFb})
    );

    fixture.detectChanges();
    
    component.submit();
    expect(mockSnackBar.open).toHaveBeenCalledTimes(1);
    expect(mockSessionApiService.update).toHaveBeenCalledTimes(1);
    expect(mockRouter.navigate).toHaveBeenCalledTimes(1);
  });
});
