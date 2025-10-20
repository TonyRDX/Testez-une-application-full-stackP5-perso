import { TestBed } from '@angular/core/testing';
import { expect } from '@jest/globals';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { SessionApiService } from './session-api.service';
import { Session } from '../interfaces/session.interface';

describe('SessionsService', () => {
  let service: SessionApiService;
  let httpMock: HttpTestingController;
  let mockSessions: Session[];

  const httpClientMock = {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports:[
        HttpClientTestingModule
      ]
    });
    service = TestBed.inject(SessionApiService);
    httpMock = TestBed.inject(HttpTestingController);

    mockSessions = [
      {
        id: 1,
        name: 'Angular',
        description: 'Formation',
        date: new Date('2025-10-19'),
        teacher_id: 3,
        users: [1, 2],
        createdAt: new Date('2025-10-01'),
        updatedAt: new Date('2025-10-10'),
      },
    ];
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all sessions', () => {
    service.all().subscribe((sessions) => {
      expect(sessions).toEqual(mockSessions);
    });

    const req = httpMock.expectOne('api/session');
    expect(req.request.method).toBe('GET');
    req.flush(mockSessions);
  });

  // it('should delete a session', () => {
  //   service.delete('1').subscribe((res) => {
  //     expect(res).toEqual({ success: true });
  //   });

  //   const req = httpMock.expectOne('api/session/1');
  //   expect(req.request.method).toBe('DELETE');
  //   req.flush({ success: true });
  // });

  it('should create a session', () => {
    service.create(mockSessions[0]).subscribe((res) => {
      expect(res).toEqual({ success: true });
    });

    const req = httpMock.expectOne('api/session');
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });
  });

  it('should update a session', () => {
    service.update("1", mockSessions[0]).subscribe((res) => {
      expect(res).toEqual({ success: true });
    });

    const req = httpMock.expectOne('api/session/1');
    expect(req.request.method).toBe('PUT');
    req.flush({ success: true });
  });

  // it('should participate', () => {
  //   service.participate("1", "2").subscribe((res) => {
  //     expect(res).toEqual({ success: true });
  //   });

  //   const req = httpMock.expectOne('api/session/1/participate/2');
  //   expect(req.request.method).toBe('POST');
  //   req.flush({ success: true });
  // });

  // it('should unparticipate', () => {
  //   service.unParticipate("1", "2").subscribe((res) => {
  //     expect(res).toEqual({ success: true });
  //   });

  //   const req = httpMock.expectOne('api/session/1/participate/2');
  //   expect(req.request.method).toBe('DELETE');
  //   req.flush({ success: true });
  // });
});
